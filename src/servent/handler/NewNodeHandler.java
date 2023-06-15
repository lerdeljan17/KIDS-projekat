package servent.handler;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public NewNodeHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
				
				int newNodePort = clientMessage.getSenderPort();
				ServentInfo newNodeInfo = new ServentInfo("localhost", newNodePort);
				
				//check if the new node collides with another existing node.
				if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
					
					Message sry = new SorryMessage(AppConfig.myServentInfo, clientMessage.getSender());
					MessageUtil.sendMessage(sry);
					return;
				}
				
				NewNodeMessage message = (NewNodeMessage) clientMessage;
				int rbs = message.getRbs();
				
				if(rbs == AppConfig.myServentInfo.getChordId()) {
					mutex.lock();
				}
				
				//check if he is my predecessor
				boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
				
				if (isMyPred) { //if yes, prepare and send welcome message
					
					ServentInfo hisPred = AppConfig.chordState.getPredecessor();
					if (hisPred == null) {

						hisPred = AppConfig.myServentInfo;
					}
					

					AppConfig.chordState.setPredecessor(newNodeInfo);
					

					
					Map<Integer, List<MyFile>> myValuesData = AppConfig.chordState.getFileValueMap();
					Map<Integer, List<MyFile>> hisValuesData = new HashMap<>();
					
					int myId = AppConfig.myServentInfo.getChordId();
					int hisPredId = hisPred.getChordId();
					int newNodeId = newNodeInfo.getChordId();
					
				
					for (Entry<Integer, List<MyFile>> valueEntry : myValuesData.entrySet()) {
						
						if (hisPredId == myId) { //i am first and he is second
							if (myId < newNodeId) {
								if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > myId) {
									hisValuesData.put(valueEntry.getKey(), valueEntry.getValue());
								}
							} else {
								if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > myId) {
									hisValuesData.put(valueEntry.getKey(), valueEntry.getValue());
								}
							}
						}
						
						if (hisPredId < myId) { //my old predecesor was before me
							
							if (valueEntry.getKey() <= newNodeId) {
								hisValuesData.put(valueEntry.getKey(), valueEntry.getValue());
							}
							
						} else { //my old predecesor was after me

							if (hisPredId > newNodeId) { //new node overflow
								
								if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > hisPredId) {
									hisValuesData.put(valueEntry.getKey(), valueEntry.getValue());
								}
								
							} else { //no new node overflow
								
								if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > hisPredId) {
									hisValuesData.put(valueEntry.getKey(), valueEntry.getValue());
								}
								
							}	
						}
					}
					
					for (Integer key : hisValuesData.keySet()) { //remove his values from my map
						
						for(MyFile d : myValuesData.get(key)) {
							String fileName = AppConfig.myServentInfo.getStorage() + File.separator + d.getName();
							File file = new File(fileName);
							file.delete();
							
							AppConfig.timestampedErrorPrint("REMOVING " + fileName + " from node " + AppConfig.myServentInfo.getListenerPort());
						}
						
						myValuesData.remove(key);
					}
					
					
					AppConfig.chordState.setFileValueMap(myValuesData);
					
					WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo, newNodeInfo, hisValuesData);
					MessageUtil.sendMessage(wm);
					
				} else { //if he is not my predecessor, let someone else take care of it
					
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
					
					NewNodeMessage nnm = new NewNodeMessage(newNodeInfo, nextNode, rbs);
					MessageUtil.sendMessage(nnm);
				}
				
			} else {
				AppConfig.timestampedErrorPrint("NEW_NODE handler got wrong message");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in NewNodeHandler. Message : " + e.getMessage() + "Error : " + Arrays.toString(e.getStackTrace()));
		}

	}

}
