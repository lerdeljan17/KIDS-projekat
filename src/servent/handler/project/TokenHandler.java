package servent.handler.project; 

import java.util.Arrays;

import app.AppConfig;
import app.ServentInfo;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.TokenMessage;
import servent.message.util.MessageUtil;

public class TokenHandler implements MessageHandler{

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public TokenHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {
		
		try {
			
			if(MessageType.TOKEN == clientMessage.getMessageType()) {
				
				TokenMessage message = (TokenMessage) clientMessage;
				
				if(message.getTokenReceiver().getChordId() == AppConfig.myServentInfo.getChordId()) {
					
					AppConfig.timestampedErrorPrint("Node " + AppConfig.myServentInfo.getChordId() + " receieved Token from " + message.getSender().getChordId());
					
					SuzukiKasamiMutex.setToken(message);
					
				} else {
				
					AppConfig.timestampedErrorPrint("Node " + AppConfig.myServentInfo.getChordId() + " received Token from " + message.getSender().getChordId());

					ServentInfo tokenReceiver = message.getTokenReceiver();
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(tokenReceiver.getChordId());
					
					TokenMessage token = message.createNewToken(AppConfig.myServentInfo, receiver, tokenReceiver);
					MessageUtil.sendMessage(token);
					
				}
				
				
			} else {
				AppConfig.timestampedErrorPrint("Token handler got a message that is not TOKEN");
			}
			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in TokenHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
		
		
	}

}
