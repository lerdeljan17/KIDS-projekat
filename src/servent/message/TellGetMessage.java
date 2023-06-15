package servent.message;

import app.ServentInfo;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;

	public TellGetMessage(ServentInfo senderPort, ServentInfo receiverPort, int key, int value) {
		super(MessageType.TELL_GET, senderPort, receiverPort, key + ":" + value);
	}
}
