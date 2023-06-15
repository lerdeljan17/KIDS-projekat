package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class RemoveFileTellMessage extends BasicMessage {

	private ServentInfo originalSender;
	private MyFile myFile;
	private String name;
	
	public RemoveFileTellMessage(ServentInfo sender, ServentInfo receiver, String name, MyFile myFile, ServentInfo originalSender) {
		super(MessageType.REMOVE_FILE_TELL, sender, receiver);
		this.myFile = myFile;
		this.originalSender = originalSender;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public MyFile getData() {
		return myFile;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}

}
