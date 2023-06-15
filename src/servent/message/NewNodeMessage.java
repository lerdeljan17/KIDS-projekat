package servent.message;

import app.ServentInfo;

public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;
	
	private int rbs;

	public NewNodeMessage(ServentInfo senderPort, ServentInfo receiverPort, int rbs) {
		super(MessageType.NEW_NODE, senderPort, receiverPort);
		this.rbs = rbs;
	}
	
	public int getRbs() {
		return rbs;
	}
}
