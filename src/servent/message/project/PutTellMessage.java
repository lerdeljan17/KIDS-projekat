package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PutTellMessage extends BasicMessage {
	
	private ServentInfo originalSender;
	private MyFile myFile;

	public PutTellMessage(ServentInfo sender, ServentInfo receiver, MyFile myFile, ServentInfo originalSender) {
		super(MessageType.PUT_TELL, sender, receiver);
		this.originalSender = originalSender;
		this.myFile = myFile;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}
	
	public MyFile getData() {
		return myFile;
	}
	
}
