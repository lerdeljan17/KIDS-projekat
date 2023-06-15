package servent.handler.project;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.ConflictMessage;
import servent.message.util.MessageUtil;

import java.util.Arrays;
import java.util.Optional;

public class ConflictHandler implements MessageHandler{
	
	private Message clientMessage;
	private SuzukiKasamiMutex mutex;

	public ConflictHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}

	@Override
	public void run() {
		
		try {
			
			if(MessageType.CONFLICT == clientMessage.getMessageType()) {
				
				ConflictMessage message = (ConflictMessage) clientMessage;
				
				if(message.getOriginalSender().getChordId() == AppConfig.myServentInfo.getChordId()) {

					MyFile myFile = message.getStorageVersion();
					AppConfig.chordState.currentlyWorkingOnForCommit.put(myFile.getName().split("_")[0], Optional.ofNullable(message));

					boolean resolved = ChordState.checkAndResolveConflicts();
					if (resolved){
						mutex.unlock(false);
					}

				} else {
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());
					ConflictMessage newMessage = new ConflictMessage(AppConfig.myServentInfo, receiver, message.getOriginalSender(),message.getMyVersion(),message.getStorageVersion());
					MessageUtil.sendMessage(newMessage);
					
				}
				
			} else {
				AppConfig.timestampedErrorPrint("Conflict handler got a message that is not CONFLICT");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in ConflictHanlder. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
