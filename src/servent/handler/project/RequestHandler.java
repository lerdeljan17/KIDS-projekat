package servent.handler.project;

import java.util.Arrays;

import app.AppConfig;
import app.ServentInfo;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.RequestMessage;
import servent.message.project.TokenMessage;
import servent.message.util.MessageUtil;

public class RequestHandler implements MessageHandler {

	private Message message;
	private SuzukiKasamiMutex mutex;
		
	public RequestHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		this.message = clientMessage;
		this.mutex = mutex;
	}

	@Override
	public void run() {
	
		try {
			
			if (message.getMessageType() == MessageType.REQUEST) {
				
				RequestMessage clientMessage = (RequestMessage)message;
				
				if(clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
					
					if(!mutex.messageIsOutdated(clientMessage.getSender().getChordId(), clientMessage.getSequenceNumber())) {
						
						if(mutex.getHasToken().get() && !mutex.getIsInCS().get() && mutex.checkOutstandingRequest(clientMessage.getSender().getChordId())) {
							
							ServentInfo tokenReceiver = clientMessage.getSender();
							
							ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(tokenReceiver.getChordId());
							
							TokenMessage token = mutex.getTokenMessage().createNewToken(AppConfig.myServentInfo, receiver, tokenReceiver);
							MessageUtil.sendMessage(token);
							
							mutex.getHasToken().set(false);
							AppConfig.timestampedErrorPrint("RequestCSHandler : Node with ID " + AppConfig.myServentInfo.getChordId() + " sent TOKEN to node with ID " + receiver.getChordId());
							
						}
	
					}
					
					RequestMessage newMessage = new RequestMessage(clientMessage.getSender(), AppConfig.chordState.getSuccessorTable()[0], clientMessage.getSequenceNumber());
					MessageUtil.sendMessage(newMessage);
					
				} 
				
			} else {
				AppConfig.timestampedErrorPrint("Request CS handler got a message that is not a REQUEST_CS");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in RequestCSHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
