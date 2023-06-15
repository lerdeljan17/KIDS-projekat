package servent.message.project;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TokenMessage extends BasicMessage{
	
	private List<Integer> LN = new CopyOnWriteArrayList<Integer>();
	private BlockingQueue<ServentInfo> ids;
	private ServentInfo tokenReceiver;
	
	public TokenMessage(ServentInfo sender, ServentInfo receiver, ServentInfo tokenReceiver) {
		super(MessageType.TOKEN, sender, receiver);
		ids = new ArrayBlockingQueue<ServentInfo>(AppConfig.SERVENT_COUNT);
		this.tokenReceiver = tokenReceiver;
		
		for (int i = 0; i < ChordState.CHORD_SIZE; i++) {
			LN.add(0);
		}
	}
	
	public TokenMessage(ServentInfo sender, ServentInfo receiver, ServentInfo tokenReceiver, List<Integer> LN, BlockingQueue<ServentInfo> ids) {
		super(MessageType.TOKEN, sender, receiver);
		this.tokenReceiver = tokenReceiver;
		this.LN = LN;
		this.ids = ids;
	}

	public TokenMessage createNewToken(ServentInfo newSender, ServentInfo newReciver, ServentInfo newTokenReceiver) {
		return new TokenMessage(newSender, newReciver, newTokenReceiver, new CopyOnWriteArrayList<Integer>(this.LN), ids);
	}
	
	public List<Integer> getLN() {
		return LN;
	}
	
	public Queue<ServentInfo> getIds() {
		return ids;
	}
	
	public ServentInfo getTokenReceiver() {
		return tokenReceiver;
	}

}