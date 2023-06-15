package servent.message.project;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

public class RemoveUpdateMessage extends BasicMessage{
	
	private ServentInfo toRemove;
	
	public RemoveUpdateMessage(ServentInfo sender, ServentInfo receiver, ServentInfo toRemove) {
		super(MessageType.REMOVE_UPDATE, sender, receiver);
		this.toRemove = toRemove;
	}

	public ServentInfo getToRemove() {
		return toRemove;
	}
}
