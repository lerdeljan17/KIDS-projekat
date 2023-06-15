package servent.message.project;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ConnectedMessage extends BasicMessage {
	
	private int rbs;

	public ConnectedMessage(ServentInfo sender, ServentInfo receiver, int rbs) {
		super(MessageType.CONNECTED, sender, receiver);
		this.rbs = rbs;
	}
	
	public int getRbs() {
		return rbs;
	}

}
