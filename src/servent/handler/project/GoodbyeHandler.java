package servent.handler.project;

import app.AppConfig;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.GoodbyeMessage;
import servent.message.project.RemoveUpdateMessage;
import servent.message.util.MessageUtil;

public class GoodbyeHandler implements MessageHandler{

	private Message message;
	private SuzukiKasamiMutex mutex;
	
	public GoodbyeHandler(Message message, SuzukiKasamiMutex mutex) {
		this.message = message;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {
		
		if(message.getMessageType() == MessageType.GOODBYE) {
			

			GoodbyeMessage goodbye = (GoodbyeMessage) message;
			AppConfig.chordState.acceptingData(goodbye.getValues(), goodbye.getPredecessor());
			
			RemoveUpdateMessage update = new RemoveUpdateMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNode(), goodbye.getSender());
			MessageUtil.sendMessage(update);
			
		} else {
			AppConfig.timestampedErrorPrint("Goodbye handler got a message that is not a GOODBYE");
		}
		
	}

}
