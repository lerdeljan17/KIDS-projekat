package servent.message;

import app.ServentInfo;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	public UpdateMessage(ServentInfo senderPort, ServentInfo receiverPort, String text) {
		super(MessageType.UPDATE, senderPort, receiverPort, text);
	}
}
