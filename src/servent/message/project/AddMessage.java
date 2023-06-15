package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class AddMessage extends BasicMessage{
	
	private ServentInfo originalSender;
	private MyFile myFile;
	private int key;

	public AddMessage(ServentInfo sender, ServentInfo receiver, int key, MyFile myFile, ServentInfo originalSender) {
		super(MessageType.ADD, sender, receiver);
		this.originalSender = originalSender;
		this.key = key;
		this.myFile = myFile;
	}
	
	public int getKey() {
		return key;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}
	
	public MyFile getData() {
		return myFile;
	}

}
