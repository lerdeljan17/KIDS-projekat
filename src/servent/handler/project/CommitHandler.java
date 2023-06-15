package servent.handler.project;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.CommitMessage;

import java.util.Arrays;

public class CommitHandler implements MessageHandler{
	
private Message clientMessage;
	
	public CommitHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		
		try {
			
			if(MessageType.COMMIT == clientMessage.getMessageType()) {
				
				CommitMessage message = (CommitMessage) clientMessage;
				
				AppConfig.chordState.updateFile(message.getKey(), message.getData(), message.getOriginalSender());
				
			} else {
				AppConfig.timestampedErrorPrint("Commit handler got a message that is not COMMIT");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in CommitHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
