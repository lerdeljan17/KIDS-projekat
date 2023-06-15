package servent.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.project.ConnectedMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.UPDATE) {
				
				if (clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
					
					ServentInfo newNodInfo = new ServentInfo("localhost", clientMessage.getSenderPort());
					List<ServentInfo> newNodes = new ArrayList<>();
					newNodes.add(newNodInfo);
					
					AppConfig.chordState.addNodes(newNodes);
					
					String newMessageText = "";
					if (clientMessage.getMessageText().equals("")) {
						newMessageText = String.valueOf(AppConfig.myServentInfo.getListenerPort());
					} else {
						newMessageText = clientMessage.getMessageText() + "," + AppConfig.myServentInfo.getListenerPort();
					}
					
					Message nextUpdate = new UpdateMessage(clientMessage.getSender(), AppConfig.chordState.getNextNode(), newMessageText);
					MessageUtil.sendMessage(nextUpdate);
					
				} else {
					
					String messageText = clientMessage.getMessageText();
					String[] ports = messageText.split(",");
					
					List<ServentInfo> allNodes = new ArrayList<>();
					for (String port : ports) {
						allNodes.add(new ServentInfo("localhost", Integer.parseInt(port)));
					}
					AppConfig.chordState.addNodes(allNodes);
					
					
					int rbs = AppConfig.myServentInfo.getRbs();
					if(rbs == -1) {
						AppConfig.timestampedErrorPrint("Somehow rbs for Node with ID " + AppConfig.myServentInfo.getChordId() + " is -1.");
						return;
					}
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(rbs);
					
					ConnectedMessage newMessage = new ConnectedMessage(AppConfig.myServentInfo, receiver, rbs);
					MessageUtil.sendMessage(newMessage);
					
					AppConfig.chordState.notifyBootstrap();
					
				}
			} else {
				AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in UpdateHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
