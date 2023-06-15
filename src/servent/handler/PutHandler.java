package servent.handler;

import java.util.Arrays;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PutMessage;

public class PutHandler implements MessageHandler {

	private Message clientMessage;
	
	public PutHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.PUT) {
				
				PutMessage message = (PutMessage) clientMessage;
				
				String[] splitText = clientMessage.getMessageText().split(":");
				if (splitText.length == 2) {
					int key = 0;
					int value = 0;
					
					try {
						key = Integer.parseInt(splitText[0]);
						value = Integer.parseInt(splitText[1]);
					
						AppConfig.chordState.putValue(key, value, message.getOriginalSender());
						
					} catch (NumberFormatException e) {
						AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
					}
				} else {
					AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
				}
				
				
			} else {
				AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in PutHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}

	}

}
