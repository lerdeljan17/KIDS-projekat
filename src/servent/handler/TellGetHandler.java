package servent.handler;

import java.util.Arrays;

import app.AppConfig;
import mutex.SuzukiKasamiMutex;
import servent.message.Message;
import servent.message.MessageType;

public class TellGetHandler implements MessageHandler {

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public TellGetHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.TELL_GET) {
				
				String parts[] = clientMessage.getMessageText().split(":");
				
				if (parts.length == 2) {
					try {
						int key = Integer.parseInt(parts[0]);
						int value = Integer.parseInt(parts[1]);
						if (value == -1) {
							AppConfig.timestampedStandardPrint("No such key: " + key);
						} else {
							AppConfig.timestampedStandardPrint(clientMessage.getMessageText());
						}
					} catch (NumberFormatException e) {
						AppConfig.timestampedErrorPrint("Got TELL_GET message with bad text: " + clientMessage.getMessageText());
					}
				} else {
					AppConfig.timestampedErrorPrint("Got TELL_GET message with bad text: " + clientMessage.getMessageText());
				}
				
				mutex.unlock(false);
			} else {
				AppConfig.timestampedErrorPrint("Tell get handler got a message that is not TELL_GET");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in TellGetHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
