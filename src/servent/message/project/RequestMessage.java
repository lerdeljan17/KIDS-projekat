package servent.message.project;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class RequestMessage extends BasicMessage{

	private int sequenceNumber;
	
	public RequestMessage(ServentInfo sender, ServentInfo receiver, int sn) {
		super(MessageType.REQUEST, sender, receiver);
		this.sequenceNumber = sn;
	}
	
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

}
