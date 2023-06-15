package servent.handler.project;

import java.util.Arrays;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.AddMessage;

public class AddHandler implements MessageHandler{
	
	private Message clientMessage;
	
	public AddHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {

		try {
			
			if(MessageType.ADD == clientMessage.getMessageType()) {
				
				AddMessage message = (AddMessage) clientMessage;
				
				AppConfig.timestampedErrorPrint("AddHandler. File [ " + message.getData().getName() + " ]. Key " + message.getKey());
				
				AppConfig.chordState.addFile(message.getKey(), message.getData(), message.getOriginalSender());
				
			} else {
				AppConfig.timestampedErrorPrint("Add handler got a message that is not ADD");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in AddHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
