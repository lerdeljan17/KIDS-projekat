package servent.handler.project;

import java.util.Arrays;
import java.util.Optional;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.RemoveFileMessage;
import servent.message.project.RemoveFileTellMessage;
import servent.message.util.MessageUtil;

public class RemoveFileTellHandler implements MessageHandler {

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public RemoveFileTellHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {

		try {
			
			if(MessageType.REMOVE_FILE_TELL == clientMessage.getMessageType()) {
				
				RemoveFileTellMessage message = (RemoveFileTellMessage) clientMessage;
				
				if(AppConfig.myServentInfo.getChordId() == message.getOriginalSender().getChordId()) {
					
					if(message.getData() == null) {
						
						ChordState.currentlyWorkingOn.remove(message.getName().split("_")[0]);
						
						if(ChordState.currentlyWorkingOn.isEmpty()) {
							mutex.unlock(false);
						}
						
					} else {
						
						ChordState.currentlyWorkingOn.remove(message.getName());
						
						String parts[] = message.getData().getContent().split(",");
						

						
						for(String s : parts) {


							
							ChordState.currentlyWorkingOn.put(s, Optional.empty());
							
							int key = ChordState.chordStringHash(s);
							ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
							

							
							RemoveFileMessage newMessage = new RemoveFileMessage(AppConfig.myServentInfo, receiver, key, s, AppConfig.myServentInfo);
							MessageUtil.sendMessage(newMessage);
						}
						
						if(ChordState.currentlyWorkingOn.isEmpty()) {
							mutex.unlock(false);
						}
					}
					
				} else {
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());
					
					RemoveFileTellMessage newMessage = new RemoveFileTellMessage(AppConfig.myServentInfo, receiver, message.getName(), message.getData(), message.getOriginalSender());
					MessageUtil.sendMessage(newMessage);
					
				}
				
				
			} else {
				AppConfig.timestampedErrorPrint("RemoveFileTell handler got a message that is not REMOVE_FILE_TELL");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
