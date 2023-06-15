package servent.handler;

import java.util.Arrays;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskGetMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;

public class AskGetHandler implements MessageHandler {

	private Message clientMessage;
	
	public AskGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.ASK_GET) {
				
				try {
					
					//9 : Proverava da li je kljuc nas ako jeste onda salje TellGet poruku ako nije onda prosledjuje dalje.
					int key = Integer.parseInt(clientMessage.getMessageText());
					if (AppConfig.chordState.isKeyMine(key)) {
						Map<Integer, Integer> valueMap = AppConfig.chordState.getValueMap(); 
						int value = -1;
						
						if (valueMap.containsKey(key)) {
							value = valueMap.get(key);
						}
						
						TellGetMessage tgm = new TellGetMessage(AppConfig.myServentInfo, clientMessage.getSender(), key, value);
						MessageUtil.sendMessage(tgm);
					} else {
						ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
						AskGetMessage agm = new AskGetMessage(clientMessage.getSender(), nextNode, clientMessage.getMessageText());
						MessageUtil.sendMessage(agm);
					}
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint("Got ask get with bad text: " + clientMessage.getMessageText());
				}
				
			} else {
				AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in AskGetHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}

	}

}