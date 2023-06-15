package servent.handler.project;

import java.util.Arrays;

import app.AppConfig;
import app.ServentInfo;
import model.MyFile;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.PullMessage;
import servent.message.project.PullTellMessage;
import servent.message.util.MessageUtil;

public class PullHandler implements MessageHandler{
	
	private Message clientMessage;
	
	public PullHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
		
			if(MessageType.PULL == clientMessage.getMessageType()) {
				
				PullMessage message = (PullMessage) clientMessage;
				
				int key = message.getKey();
				
				AppConfig.timestampedErrorPrint("PullHandler for " + key + " : " + message.getName() + "   node : " + AppConfig.myServentInfo.getListenerPort() );
				
				if(AppConfig.chordState.isKeyMine(key)) {
					
					AppConfig.timestampedErrorPrint("Node " + AppConfig.myServentInfo.getListenerPort() + " " + AppConfig.chordState.storageData.keySet());
					
					MyFile myFile = AppConfig.chordState.pullData(key, message.getName(), message.getVersion());
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());

					
					PullTellMessage pullTellMessage = new PullTellMessage(AppConfig.myServentInfo, receiver, myFile, message.getOriginalSender(),message.getName());
					MessageUtil.sendMessage(pullTellMessage);
					
				} else {

					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
					PullMessage newMessage = new PullMessage(AppConfig.myServentInfo, receiver, key, message.getName(), message.getVersion(), message.getOriginalSender());
					MessageUtil.sendMessage(newMessage);
				}
				 
			} else {
				AppConfig.timestampedErrorPrint("Pull handler got a message that is not PULL");
			}			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in PullHandler. Message : " + e.getMessage() + "Error : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
