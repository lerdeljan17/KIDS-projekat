package servent.handler.project;

import java.util.*;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.PullMessage;
import servent.message.project.PullTellMessage;
import servent.message.util.MessageUtil;

public class PullTellHandler implements MessageHandler {

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public PullTellHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {

		try {
		
			if(MessageType.PULL_TELL == clientMessage.getMessageType()) {
				
				PullTellMessage message = (PullTellMessage) clientMessage;
				
				if(message.getOriginalSender().getChordId() == AppConfig.myServentInfo.getChordId()) {
		
					MyFile myFile = message.getData();
					
					if(myFile == null) {
						ChordState.currentlyWorkingOn.remove(message.getFileName().split("_")[0]);
						AppConfig.timestampedErrorPrint("User asked for file " + message.getFileName() + " witch does not exist in system!");
					} else {
						ChordState.currentlyWorkingOn.put(myFile.getName().split("_")[0], Optional.ofNullable(myFile));

						
						if(myFile.getName().split("_")[1].equals("dir")) {
							String parts[] = myFile.getContent().split(",");
							
							for(String s : parts) {
								ChordState.currentlyWorkingOn.put(s, Optional.empty());
								
								int key = ChordState.chordStringHash(s);
								
								ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);

								PullMessage newPullMessage = new PullMessage(AppConfig.myServentInfo, receiver, key, s, -1, AppConfig.myServentInfo);
								MessageUtil.sendMessage(newPullMessage);
							}
							
						}
					}
					
					
					if(AppConfig.chordState.isMapCompleted()) {

						List<String> list = new ArrayList<String>(ChordState.currentlyWorkingOn.keySet());
						Collections.sort(list);

						for(String s : list) {
							MyFile d = ChordState.currentlyWorkingOn.get(s).get();
							MyFile.myFileToWorkingFile(d);
						}

						ChordState.currentlyWorkingOn.clear();
						mutex.unlock(false);
					}
					
				} else {
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());
					PullTellMessage newMessage = new PullTellMessage(AppConfig.myServentInfo, receiver, message.getData(), message.getOriginalSender(),message.getFileName());
					MessageUtil.sendMessage(newMessage);
				}				
			} else {
				AppConfig.timestampedErrorPrint("PullTell handler got a message that is not PULL_TELL");
			}			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in PullTellHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
