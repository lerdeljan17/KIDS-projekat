package servent.message;

import app.ServentInfo;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	private ServentInfo originalSender;
	
	public PutMessage(ServentInfo senderPort, ServentInfo receiverPort, int key, int value, ServentInfo originalSender) {
		super(MessageType.PUT, senderPort, receiverPort, key + ":" + value);
		this.originalSender = originalSender;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}
	
	public void setOriginalSender(ServentInfo originalSender) {
		this.originalSender = originalSender;
	}
}
