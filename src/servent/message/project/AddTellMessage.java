package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class AddTellMessage extends BasicMessage{

	private ServentInfo originalSender;
	private MyFile myFile;
	
	public AddTellMessage(ServentInfo sender, ServentInfo receiver, MyFile myFile, ServentInfo originalSender) {
		super(MessageType.ADD_TELL, sender, receiver);
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
