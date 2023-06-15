package servent.handler.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.GoodbyeTellMessage;
import servent.message.project.RemoveUpdateMessage;
import servent.message.util.MessageUtil;

public class RemoveUpdateHandler implements MessageHandler{

	private Message message;
	
	public RemoveUpdateHandler(Message message) {
		this.message = message;
	}
	
	@Override
	public void run() {

		try {
			
			if(message.getMessageType() == MessageType.REMOVE_UPDATE) {			
				
				RemoveUpdateMessage update = (RemoveUpdateMessage) message;
				
				List<ServentInfo> nodes = new ArrayList<ServentInfo>();
				nodes.add(update.getToRemove());
				AppConfig.chordState.removeNode(nodes);
				
				if(update.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
				
					RemoveUpdateMessage newMessage = new RemoveUpdateMessage(update.getSender(), AppConfig.chordState.getNextNode(), update.getToRemove());
					MessageUtil.sendMessage(newMessage);
					
				} else if ((update.getSenderPort() == AppConfig.myServentInfo.getListenerPort()) || (AppConfig.chordState.getAllNodeInfo().size() == 0)){
					
					GoodbyeTellMessage tellMessage = new GoodbyeTellMessage(AppConfig.myServentInfo, update.getToRemove());
					MessageUtil.sendMessage(tellMessage);
				}
				
			} else {
				AppConfig.timestampedErrorPrint("RemoveUpdateHandler got a message that's not REMOVE_UPDATE");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in RemoveUpdateHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
