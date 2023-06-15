package servent.message.project;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PullMessage extends BasicMessage{

	private ServentInfo originalSender;
	private int key;
	private String name;
	private int version;
	
	public PullMessage(ServentInfo sender, ServentInfo receiver, int key, String name, int version, ServentInfo orginalSender) {
		super(MessageType.PULL, sender, receiver);
		this.originalSender = orginalSender;
		this.key = key;
		this.name = name;
		this.version = version;
	}
	
	public ServentInfo getOriginalSender() {
		return originalSender;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public int getVersion() {
		return version;
	}

}
