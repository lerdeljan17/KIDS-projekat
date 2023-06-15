package servent.handler.project;

import java.util.Arrays;
import java.util.Optional;

import app.AppConfig;
import app.ServentInfo;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.PutTellMessage;
import servent.message.util.MessageUtil;

public class PutTellHandler implements MessageHandler{

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public PutTellHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}

	@Override
	public void run() {
		
		try {
			
			if(MessageType.PUT_TELL == clientMessage.getMessageType()) {
				
				PutTellMessage message = (PutTellMessage) clientMessage;
				
				if(message.getOriginalSender().getChordId() == AppConfig.myServentInfo.getChordId()) {
					
					MyFile myFile = message.getData();

					AppConfig.chordState.currentlyWorkingOn.put(myFile.getName().split("_")[0], Optional.ofNullable(myFile));
					
					if(AppConfig.chordState.isMapCompleted()) {
						AppConfig.chordState.currentlyWorkingOn.clear();
						mutex.unlock(false);
					}					
					
				} else {
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());
					PutTellMessage newMessage = new PutTellMessage(AppConfig.myServentInfo, receiver, message.getData(), message.getOriginalSender());
					MessageUtil.sendMessage(newMessage);
				}
				
			} else {
				AppConfig.timestampedErrorPrint("PutTell handler got a message that is not PUT_TELL");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in PutTellHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}
	
	
}
