package app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import model.MyFile;
import servent.message.AskGetMessage;
import servent.message.PutMessage;
import servent.message.WelcomeMessage;
import servent.message.project.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}

	public static int chordStringHash(String toHash) {
		BigInteger hash = new BigInteger("7");
		for (int i = 0; i < toHash.length(); i++) {
			hash = hash.multiply(BigInteger.valueOf(31)).add(BigInteger.valueOf(toHash.charAt(i)));
		}
//		hash = hash.multiply(BigInteger.valueOf(61));
		hash = hash.mod(BigInteger.valueOf(CHORD_SIZE));
		return  hash.intValue();
	}
	
	

	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;
	
 	public static Map<Integer, List<String>> workingData;
 	public static Map<Integer, List<String>> storageData;

	public static Map<String, Optional<MyFile>> currentlyWorkingOn = new ConcurrentHashMap<>();
	//potrebno da prima message zbog konflikta
	public static  Map<String, Optional<ConflictMessage>> currentlyWorkingOnForCommit = new ConcurrentHashMap<>();

	public static AtomicBoolean currentlyResolvingConflict = new AtomicBoolean(false);
	public static String currentFileName;
	//koristi se kod razresavanja konflikta da bi se moglo znati koji se trenutno posmatra
	//predstavlja poruku koja ima moju i warehous verziju
	public static ConflictMessage currentData;
 	

 	private Map<Integer, List<MyFile>> fileValueMap;
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();
		

		storageData = new ConcurrentHashMap<Integer, List<String>>();
		workingData = new ConcurrentHashMap<Integer, List<String>>();
		



		fileValueMap = new HashMap<>();
//		currentlyWorkingOn = new ConcurrentHashMap<>();
//		currentlyWorkingOnForCommit = new ConcurrentHashMap<>();
	}


	public static boolean checkAndResolveConflicts() throws InterruptedException {
		//moze unlock
		if (currentlyWorkingOnForCommit.isEmpty())return true;
		// nema jos svih odg, nemoze unlock
		if (!AppConfig.chordState.isCommitMapCompleted()) return false;

		if(AppConfig.chordState.isCommitMapCompleted() && !ChordState.currentlyWorkingOnForCommit.isEmpty()) {
			for (Map.Entry<String,Optional<ConflictMessage>> entry: AppConfig.chordState.currentlyWorkingOnForCommit.entrySet()){
				ChordState.currentlyResolvingConflict.set(true);
				ChordState.currentData = entry.getValue().get();
				//name bez verzije
				ChordState.currentFileName = entry.getKey();
//				System.out.println("Conflict 1happend! with file "+ entry.getKey() + " Please choose action command.\n1. view\n2. push\n3. pull\n");
				AppConfig.timestampedStandardPrint("Conflict happened! with file "+ entry.getKey() + " Please choose action command. 1. view 2. push 3. pull");

				//blokiraj se sve dok se ne urade push ili pull koji ce oboriti flag
				while (ChordState.currentlyResolvingConflict.get()){
					Thread.sleep(1000);
				}
			}
			currentlyWorkingOnForCommit.clear();



//						mutex.unlock();
		}
		return true;
	}

	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		
		this.fileValueMap = welcomeMsg.getValues();
		
		for(Integer key : fileValueMap.keySet()) {
			List<MyFile> keyValues = fileValueMap.get(key);
			
			for(MyFile myFile : keyValues) {
				MyFile.myFileToStorageFile(myFile);
				
				String name = AppConfig.myServentInfo.getStorage() + File.separator + myFile.getName();
				AppConfig.timestampedErrorPrint("PERSISTING " + name + " on node " + AppConfig.myServentInfo.getListenerPort());
			}
		}
		
	}
	

	public void notifyBootstrap() {

		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			AppConfig.timestampedErrorPrint("notifyBootstrap -> Message : " + e.getMessage() + "\nErrors : " + Arrays.toString(e.getStackTrace()));
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("notifyBootstrap -> Message : " + e.getMessage() + "\nErrors : " + Arrays.toString(e.getStackTrace()));
		}

		AppConfig.timestampedStandardPrint("Bootstrap notified for node " + AppConfig.myServentInfo.getChordId());
	}
	
	
	public boolean isCollision(int chordId) {
		AppConfig.timestampedStandardPrint("ChordState -> isCollision(). allNodeInfo = " + allNodeInfo);
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		

		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		

		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
		
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				
				if (currentValue > currentId) {
					
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
					
				} else { //node id is larger
					
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		
		allNodeInfo.addAll(newNodes);
		

		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
		
		AppConfig.timestampedErrorPrint("ChordState -> addNodes() : allNodeInfo = " + allNodeInfo.toString());
	}

	public void updateFile(int key, MyFile value, ServentInfo whoDidCommitComand) {


		if(isKeyMine(key)) {

			File file = getFileByName(value.getName());

			if(file == null) {
				AppConfig.timestampedErrorPrint("File " + value.getName() + " does not exist in system!");
				return;
			}

			int versionInStorage = getVersion(file.getPath());
			int versionOfData = value.getVersion() + 1;

			if(versionInStorage >= versionOfData) {


				String contentInStorage = null;
				try {
					contentInStorage = new String(Files.readAllBytes(file.toPath()));
				} catch (IOException e) {
					AppConfig.timestampedErrorPrint("ChordState -> updateFile(). Message : " + e.getMessage() + "\nErrors : " + Arrays.toString(e.getStackTrace()));
					return;
				}

				if(contentInStorage.equals(value.getContent())) {

					//TODO poruka o zavrsenom commit-u
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(whoDidCommitComand.getChordId());
					CommitTellMessage commitTellMessage = new CommitTellMessage(AppConfig.myServentInfo,receiver,value,whoDidCommitComand);
					MessageUtil.sendMessage(commitTellMessage);

				} else {
					//TODO poruka o konfliktu


					MyFile storageVersion = MyFile.workingFileToMyFile(file,AppConfig.myServentInfo.getStorage());
					storageVersion.setVersion(Integer.parseInt(storageVersion.getName().split("_")[1]));

					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(whoDidCommitComand.getChordId());
					ConflictMessage conflictMessage = new ConflictMessage(AppConfig.myServentInfo, receiver, whoDidCommitComand,value,storageVersion);
					MessageUtil.sendMessage(conflictMessage);
				}

			} else {

				AppConfig.timestampedErrorPrint("CS: updateFile -> versionOfData" + versionOfData);
				value.setVersion(versionOfData);

				List<String> toAdd = storageData.get(key);
				toAdd.add(value.getName());

				storageData.put(key, toAdd);


				MyFile.myFileToStorageFile(value);

				//TODO poruka o zavrsenom commit-u
				ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(whoDidCommitComand.getChordId());
				CommitTellMessage commitTellMessage = new CommitTellMessage(AppConfig.myServentInfo,receiver,value,whoDidCommitComand);
				MessageUtil.sendMessage(commitTellMessage);

//				putFileInStorage(key, value, whoDidCommitComand);
			}


		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			CommitMessage commitMessage = new CommitMessage(AppConfig.myServentInfo, nextNode, key, value, whoDidCommitComand);
			MessageUtil.sendMessage(commitMessage);
		}

	}
	

	
	private int getVersion(String filePath) {
		String text = filePath.split("_")[1];
		
		int version = -1;
	
		try {
			version = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("ChordState -> getVersion(). Message : " + e.getMessage() + "\nErrors : " + Arrays.toString(e.getStackTrace()));
		}
		
		return version;
	}
	
	private String withOutVersion(String fileName) {
		return fileName.split("_")[0];
	}
	

	private File getFileByNameAndVersion(String name, int version) {
		
		String nameWithOutVersion = name.split("_")[0];
		
		File storage = new File(AppConfig.myServentInfo.getStorage());
		File returnFile = null;
		
		List<File> allFiles = getAllFiles(storage);
		
		for(File f : allFiles) {
			String path = f.getPath();
			String parts[] = path.split("_");
			String withoutVersion = parts[0];
			int fVersion = -1;

			boolean directory = false;
			if(parts[1].equals("dir")) {
				directory = true;
			} else {
				try {
					fVersion = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint("ChordState -> getFileByName(). Message : " + e.getMessage() + "\nErrors : " + Arrays.toString(e.getStackTrace()));
				}
				
				if(version == -1) {
				}
			}	

			int index = (int) (AppConfig.myServentInfo.getStorage().length() + 1);
			String fileName = withoutVersion.substring(index);
			
			if(fileName.equals(nameWithOutVersion)) {
				if(directory) {
					returnFile = f;
					break;
				}
				
				if(version == fVersion) {
					returnFile = f;
					break;
				}
				
			}
		}
		
		return returnFile;
		
	}


	private File getFileByName(String name) {
		
		String nameWithOutVersion = name.split("_")[0];
		
		File storage = new File(AppConfig.myServentInfo.getStorage());
		File returnFile = null;
		int fileVersion = -1;
		boolean directory = false;
		

		List<File> allFiles = getAllFiles(storage);
		
		for(File f : allFiles) {
			String path = f.getPath();
			String parts[] = path.split("_");
			String withoutVersion = parts[0];
			int version = -1;
			
			if(parts[1].equals("dir")) {
				directory = true;
			} else {
				try {
					version = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint("ChordState: getFileByName(). Message : " + e.getMessage() + "Errors : " + Arrays.toString(e.getStackTrace()));
				}

			}
			
			int index = (int) (AppConfig.myServentInfo.getStorage().length() + 1);
			String fileName = withoutVersion.substring(index);
			
			if(fileName.equals(nameWithOutVersion)) {
						
				if(directory) {
					returnFile = f;
					break;
				}
				
				if(returnFile == null) {
					returnFile = f;
					fileVersion = version;
				} else {
					if(fileVersion < version) {
						returnFile = f;
						fileVersion = version;
					}
				}
				
			}
		}
		
		return returnFile;
		
	}


	public Map<Integer, List<MyFile>> getFileValueMap() {
		createFileValueMap();
		return fileValueMap;
	}
	
	public void setFileValueMap(Map<Integer, List<MyFile>> fileValueMap) {
		this.fileValueMap = fileValueMap;
	}
	
	private void createFileValueMap() {
		fileValueMap.clear();
		
		File storage = new File(AppConfig.myServentInfo.getStorage());
		for(File f : getAllFiles(storage)) {
			MyFile myFile = MyFile.workingFileToMyFile(f, AppConfig.myServentInfo.getStorage());
			
			int key = ChordState.chordStringHash(myFile.getName().split("_")[0]);
			
			if(fileValueMap.containsKey(key)) {
				
				fileValueMap.get(key).add(myFile);
				
			} else {
				List<MyFile> newValue = new ArrayList<MyFile>();
				newValue.add(myFile);
				
				fileValueMap.put(key, newValue);
			}
		}
	}


	private List<File> getAllFiles(File file) {
		List<File> files = new ArrayList<File>();
		
		for(File f : file.listFiles()) {
			
			if(f.isDirectory() && f.listFiles().length > 0) {
				
				files.addAll(getAllFiles(f));
				
			} else if(!f.isDirectory()) {
				
				files.add(f);
			}
		}
		
		return files;
	}

	public void addFile(int key, MyFile value, ServentInfo whoDidAddComand) {
		
		if(isKeyMine(key)) {
			
			value.setVersion(0);
			
			List<String> valuesOfKey = storageData.get(key);
			
			if(valuesOfKey != null) {
				
				storageData.get(key).add(value.getName());
				
			} else {
				
				valuesOfKey = new ArrayList<String>();
				valuesOfKey.add(value.getName());
				storageData.put(key, valuesOfKey);
				
			}

			MyFile.myFileToStorageFile(value);


			ServentInfo receiver = getNextNodeForKey(whoDidAddComand.getChordId());
			if(AppConfig.myServentInfo.getChordId() == whoDidAddComand.getChordId()) {
				receiver = AppConfig.myServentInfo;
			}
			AddTellMessage addTellMessage = new AddTellMessage(AppConfig.myServentInfo, receiver, value, whoDidAddComand);
			MessageUtil.sendMessage(addTellMessage);
			
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			AddMessage addMessage = new AddMessage(AppConfig.myServentInfo, nextNode, key, value, whoDidAddComand);
			MessageUtil.sendMessage(addMessage);
		}
		
	}
	
	/***
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 * @param key Hash value of file we want to store.
	 * @param value Value that we want to store.
	 * @param whoDidPutCommand Node who did Put Command. Important to have so we can know who we need to send PutFinished message so we can unlock.
	 */
	public void putValue(int key, int value, ServentInfo whoDidPutCommand) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
			
			ServentInfo receiver = getNextNodeForKey(whoDidPutCommand.getChordId());
			PutTellMessage putTellMessage = new PutTellMessage(AppConfig.myServentInfo, receiver, null, whoDidPutCommand);
			MessageUtil.sendMessage(putTellMessage);
			
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo, nextNode, key, value, whoDidPutCommand);
			MessageUtil.sendMessage(pm);
		}
	}
	



	public MyFile pullData(int key, String name, int version) {
		
		if(isKeyMine(key)) {
			File file = null;	
			
			if(version == -1) {
				file = getFileByName(name);
			} else {
				file = getFileByNameAndVersion(name, version);				
			}
			
			if(file == null) {
				return null;
			}
			
			MyFile myFile = MyFile.workingFileToMyFile(file, AppConfig.myServentInfo.getStorage());
			return myFile;
			
		}
		
		return null;
	}
	

	private boolean hasFile(String name) {
		for(String s : storageData.get(ChordState.chordStringHash(name))) {
			if(s.split("_")[0].equals(name)) {
				return true;
			}
		}
		
		return false;
	}

	public int checkValue(int key, String string) {
		if(isKeyMine(key)) {
			if(storageData.containsKey(key) && hasFile(string)) {
				return 0;
			} else {
				return -1;
			}
		}
		return -2;
	}
	

	public MyFile remove(int key, String name) {
		
		List<File> filesToRemove = new ArrayList<File>();
		
		File file = new File(AppConfig.myServentInfo.getStorage());
		
		for(File f : AppConfig.chordState.getAllFiles(file)) {
			
			String parts[] = f.getPath().split("_");
			String fName = parts[0];
			int index = AppConfig.myServentInfo.getStorage().length();
			fName = fName.substring(index+1);
			
			boolean directory = false;
			if(parts.length > 1 && parts[1].equals("dir")) directory = true;
			
			if(fName.equals(name)) {
				
				AppConfig.timestampedErrorPrint("In REMOVE found file " + fName);
				
				if(directory) {
					MyFile myFile = MyFile.workingFileToMyFile(f, AppConfig.myServentInfo.getStorage());
					f.delete();
					return myFile;
				} else {
					filesToRemove.add(f);
				}
			}
		}
		
		if(filesToRemove.isEmpty()) {
			return null;
		}
		
		for(File f : filesToRemove) {
			f.delete();
		}
		
		return null;
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public int getValue(int key) {
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			} else {
				return -1;
			}
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		//9 : Za slucaj da nismo mi vlasnik onda prosledjujemo get poruku sledecem cvoru po kord pretrazi.
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo, nextNode, String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}
	

	public void turnOffNode() {
		ServentInfo sender = AppConfig.myServentInfo;
		ServentInfo reciever = this.successorTable[0];
		
		String message = "Node " + sender.getListenerPort() + " is turning off!";
		
		if(allNodeInfo.size() > 0) {
			
			GoodbyeMessage gbm = new GoodbyeMessage(sender, reciever, message, getFileValueMap(), this.predecessorInfo);
			MessageUtil.sendMessage(gbm);

			File storage = new File(AppConfig.myServentInfo.getStorage());
			for(File f : getAllFiles(storage)) {
				f.delete();
			}
				
			AppConfig.timestampedErrorPrint("turnOffNode " + message + " Goodbye Node " + reciever.getListenerPort());
		} else {

			AppConfig.timestampedErrorPrint("turnOffNode " + message);
		}
		
		
		
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Remove\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			AppConfig.timestampedErrorPrint(e.getMessage() + "Error " + Arrays.toString(e.getStackTrace()));
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint(e.getMessage() + "Errors" + Arrays.toString(e.getStackTrace()));
		}
	}
	

	public void acceptingData(Map<Integer, List<MyFile>> newValues, ServentInfo newPredecessor) {
	
		this.predecessorInfo = newPredecessor;
		
		this.fileValueMap.putAll(newValues);
				
		for(Integer key : newValues.keySet()) {
			List<MyFile> keyValues = newValues.get(key);
			
			for(MyFile myFile : keyValues) {
				MyFile.myFileToStorageFile(myFile);

			}
		}
		
	}
	

	public void removeNode(List<ServentInfo> nodes) {
		

		this.allNodeInfo.removeAll(nodes);
		
		if (allNodeInfo.size() == 0){
			setPredecessor(null);
			successorTable = new ServentInfo[chordLevel];
		}else {
			updateSuccessorTable();
		}
		
	}

	public boolean isMapCompleted() {
		AppConfig.timestampedErrorPrint("isMapCompleted -> Mapa" +currentlyWorkingOn);
		for(String s : currentlyWorkingOn.keySet()) {
			AppConfig.timestampedErrorPrint("isMapCompleted -> " + s);
			if (currentlyWorkingOn.get(s) == null)continue;
			if(currentlyWorkingOn.get(s).isEmpty()) {
				return false;
			}

		}

		return true;
	}

//	public static void main(String[] args) {
//		Map<String,Optional<Data>> map = new ConcurrentHashMap<>();
//		Data data2 = new Data("bla","c",false);
//		map.put("bla",Optional.ofNullable(data2));
//
//		Data data = null;
//		map.put("alda",Optional.ofNullable(null));
//		System.out.println(" map "+ map);
//		for(String s : map.keySet()) {
//			System.out.println("trenutni key " + s);
//			if(map.get(s).isEmpty()) {
//				System.out.println(map.get(s));
//			}
//
//		}
//
//	}


	public boolean isCommitMapCompleted() {
		for(String s : currentlyWorkingOnForCommit.keySet()) {
			AppConfig.timestampedErrorPrint("isCommitMapCompleted -> " + s);
			if (currentlyWorkingOn.get(s) == null)continue;
			if(currentlyWorkingOnForCommit.get(s).isEmpty()) {
				return false;
			}
		}

		return true;
	}
	
	public List<ServentInfo> getAllNodeInfo() {
		return allNodeInfo;
	}
	



	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public ServentInfo getNextNode() {
		return successorTable[0];
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}

	public void pushFile(int key, MyFile value, ServentInfo whoDidPushComand, String oldName) {


		if(isKeyMine(key)) {

			MyFile.pushToFile(value);

			ServentInfo receiver = getNextNodeForKey(whoDidPushComand.getChordId());
			if(AppConfig.myServentInfo.getChordId() == whoDidPushComand.getChordId()) {
				receiver = AppConfig.myServentInfo;
			}
			PushResolveTellMessage pushResolveTellMessage  = new PushResolveTellMessage(AppConfig.myServentInfo, receiver, value, whoDidPushComand,oldName);
			MessageUtil.sendMessage(pushResolveTellMessage);

		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PushResolveMessage pushResolveMessage = new PushResolveMessage(AppConfig.myServentInfo, nextNode, key, value, whoDidPushComand, oldName);
			MessageUtil.sendMessage(pushResolveMessage);
		}


	}

}
