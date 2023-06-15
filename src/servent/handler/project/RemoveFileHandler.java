package servent.handler.project;

import java.util.Arrays;

import app.AppConfig;
import app.ServentInfo;
import model.MyFile;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.RemoveFileMessage;
import servent.message.project.RemoveFileTellMessage;
import servent.message.util.MessageUtil;

public class RemoveFileHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public RemoveFileHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {

		try {

			if(MessageType.REMOVE_FILE == clientMessage.getMessageType()) {
				
				RemoveFileMessage message = (RemoveFileMessage) clientMessage;
				
				int key = message.getKey();
				
				if(AppConfig.chordState.isKeyMine(key)) {
					
					AppConfig.timestampedErrorPrint("remove() : " + message.getName() + " NODE " + AppConfig.myServentInfo.getListenerPort());
					
					MyFile myFile = AppConfig.chordState.remove(key, message.getName());
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());
					

					
					RemoveFileTellMessage tellMessage = new RemoveFileTellMessage(AppConfig.myServentInfo, receiver, message.getName(), myFile, message.getOriginalSender());
					MessageUtil.sendMessage(tellMessage);
					
				} else {
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
					RemoveFileMessage newMessage = new RemoveFileMessage(AppConfig.myServentInfo, receiver, key, message.getName(), message.getOriginalSender());
					MessageUtil.sendMessage(newMessage);
					
				}
				
				
			} else {
				AppConfig.timestampedErrorPrint("RemoveFile handler got a message that is not REMOVE_FILE");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in RemoveFileHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
