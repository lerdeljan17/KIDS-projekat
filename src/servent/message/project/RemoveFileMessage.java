package servent.message.project;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class RemoveFileMessage extends BasicMessage{

	private ServentInfo originalSender;
	private int key;
	private String name;
	
	public RemoveFileMessage(ServentInfo sender, ServentInfo receiver, int key, String name, ServentInfo originalSender) {
		super(MessageType.REMOVE_FILE, sender, receiver);
		this.key = key;
		this.name = name;
		this.originalSender = originalSender;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}
	
}
