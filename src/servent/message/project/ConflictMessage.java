package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ConflictMessage extends BasicMessage {

	private ServentInfo originalSender;
	private MyFile myVersion;
	private MyFile storageVersion;
	
	public ConflictMessage(ServentInfo sender, ServentInfo receiver, ServentInfo originalSender, MyFile myVersion, MyFile storageVersion) {
		super(MessageType.CONFLICT, sender, receiver);
		this.originalSender = originalSender;
		this.storageVersion = storageVersion;
		this.myVersion = myVersion;
	}

	public ServentInfo getOriginalSender() {
		return originalSender;
	}

	public MyFile getStorageVersion() {
		return storageVersion;
	}

	public MyFile getMyVersion() {
		return myVersion;
	}
}
