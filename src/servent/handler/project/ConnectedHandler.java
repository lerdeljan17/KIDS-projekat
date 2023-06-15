package servent.handler.project;

import java.util.Arrays;

import app.AppConfig;
import app.ServentInfo;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.ConnectedMessage;
import servent.message.util.MessageUtil;

public class ConnectedHandler implements MessageHandler{

	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	
	public ConnectedHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {
		
		try {
			
			if(MessageType.CONNECTED == clientMessage.getMessageType()) {
				
				ConnectedMessage message = (ConnectedMessage) clientMessage;
				
				if(AppConfig.myServentInfo.getChordId() == message.getRbs()) {
					
					mutex.unlock(false);
					
				} else {
					
					int rbs = message.getRbs();
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(rbs);
					
					ConnectedMessage newMessage = new ConnectedMessage(AppConfig.myServentInfo, receiver, rbs);
					MessageUtil.sendMessage(newMessage);
					
				}			
				
			} else {
				AppConfig.timestampedErrorPrint("GotIn handler got a message that's not GOT_IN");	
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in GotInHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}

