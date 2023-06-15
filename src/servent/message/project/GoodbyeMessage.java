package servent.message.project;

import java.util.List;
import java.util.Map;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class GoodbyeMessage extends BasicMessage{
	
	private Map<Integer, List<MyFile>> values;
	private ServentInfo predecessor;
	
	public GoodbyeMessage(ServentInfo sender, ServentInfo receivert, String messageText, Map<Integer, List<MyFile>> values, ServentInfo predecessor) {
		super(MessageType.GOODBYE, sender, receivert, messageText);
		this.values = values;
		this.predecessor = predecessor;
	}

	public Map<Integer, List<MyFile>> getValues() {
		return values;
	}
	
	public ServentInfo getPredecessor() {
		return predecessor;
	}
}
