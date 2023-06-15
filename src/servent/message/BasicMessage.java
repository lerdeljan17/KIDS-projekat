package servent.message;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import app.ChordState;
import app.ServentInfo;
import servent.message.project.RequestMessage;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message {

	private static final long serialVersionUID = -9075856313609777945L;
	private final MessageType type;
	private final ServentInfo sender;
	private final ServentInfo receiver;
	private final int senderPort;
	private final int receiverPort;
	private final String messageText;
	
	//This gives us a unique id - incremented in every natural constructor.
	private static AtomicInteger messageCounter = new AtomicInteger(0);
	private int messageId;
	
	public BasicMessage(MessageType type, ServentInfo sender, ServentInfo receiver) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.senderPort = sender.getListenerPort();
		this.receiverPort = receiver.getListenerPort();
		this.messageText = "";
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	public BasicMessage(MessageType type, ServentInfo sender, ServentInfo receiver, String messageText) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.senderPort = sender.getListenerPort();
		this.receiverPort = receiver.getListenerPort();
		this.messageText = messageText;
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}
	
	@Override
	public int getReceiverPort() {
		return receiverPort;
	}
	
	@Override
	public String getReceiverIpAddress() {
		return "localhost";
	}
	
	@Override
	public int getSenderPort() {
		return senderPort;
	}

	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}
	
	@Override
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
		
	/**
	 * Comparing messages is based on their unique id and the original sender port.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;
			
			if (getMessageId() == other.getMessageId() &&
				getSenderPort() == other.getSenderPort()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getSenderPort());
	}
	
	/**
	 * Returns the message in the format: <code>[sender_id|sender_port|message_id|text|type|receiver_port|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "[" + ChordState.chordHash(getSenderPort()) + "|" + getSenderPort() + "|" + getMessageId() + "|" +
					getMessageText() + "|" + getMessageType() + "|" +
					getReceiverPort() + "|" + ChordState.chordHash(getReceiverPort()) + "]";
	}

	@Override
	public ServentInfo getSender() {
		return sender;
	}

	@Override
	public ServentInfo getReceiver() {
		return receiver;
	}
	
	
	
	/**
	 * Change the message received based on ID. The receiver has to be our neighbor.
	 * Use this when you want to send a message to multiple neighbors, or when resending.
	 */

	@Override
	public Message changeReceiver(ServentInfo newReceiverId,int sn) {

		Message toReturn = null;
		if (type.equals(MessageType.REQUEST)) {
			toReturn = new RequestMessage(this.sender, newReceiverId, sn);
			toReturn.setMessageId(this.messageId);
			
		}
			
			
		return toReturn;

		
	}

}
