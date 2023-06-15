package servent.handler.project;

import java.util.Arrays;

import app.AppConfig;
import cli.CLIParser;
import mutex.SuzukiKasamiMutex;
import servent.SimpleServentListener;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.GoodbyeTellMessage;

public class GoodbyeTellHandler implements MessageHandler{
	
	private Message clientMessage;
	private SuzukiKasamiMutex mutex;
	private SimpleServentListener simpleServentListener;
	private CLIParser parser;
	
	public GoodbyeTellHandler(Message clientMessage, SuzukiKasamiMutex mutex, SimpleServentListener simpleServentListener, CLIParser parser) {
		this.clientMessage = clientMessage;
		this.mutex = mutex;
		this.parser= parser;
		this.simpleServentListener = simpleServentListener;
		
	}

	@Override
	public void run() {
		try {
			
			if(MessageType.GOODBYE_TELL == clientMessage.getMessageType()) {
				
				GoodbyeTellMessage message = (GoodbyeTellMessage) clientMessage;
				mutex.unlock(true);
				
				simpleServentListener.stop();
				parser.stop();
				
			} else {
				AppConfig.timestampedErrorPrint("GoodbyeTellHandler got a message that's not GOODBYE_TELL");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in GoodbyeTellHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
	}

}
