package servent.handler;

import java.util.Arrays;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;
	
	public WelcomeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.WELCOME) {
				
				WelcomeMessage welcomeMsg = (WelcomeMessage)clientMessage;
				
				AppConfig.chordState.init(welcomeMsg);
				
				UpdateMessage um = new UpdateMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNode(), "");
				MessageUtil.sendMessage(um);
				
			} else {
				AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in WelcomeHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}

	}

}
