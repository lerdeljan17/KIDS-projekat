package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PullTellMessage extends BasicMessage {
	
	private ServentInfo originalSender;
	private MyFile myFile;
	private String fileName;

	public PullTellMessage(ServentInfo sender, ServentInfo receiver, MyFile myFile, ServentInfo originalSender, String filename) {
		super(MessageType.PULL_TELL, sender, receiver);
		this.originalSender = originalSender;
		this.myFile = myFile;
		this.fileName = filename;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}
	
	public MyFile getData() {
		return myFile;
	}
	
	public String getFileName() {
		return fileName;
	}

}
