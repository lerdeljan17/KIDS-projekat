package servent.message.project;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class GoodbyeTellMessage extends BasicMessage{
	
	public GoodbyeTellMessage(ServentInfo sender, ServentInfo receiver) {
		super(MessageType.GOODBYE_TELL, sender, receiver);
	}
	
}
