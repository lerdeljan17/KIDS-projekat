package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import cli.CLIParser;
import mutex.SuzukiKasamiMutex;
import servent.handler.AskGetHandler;
import servent.handler.MessageHandler;
import servent.handler.NewNodeHandler;
import servent.handler.NullHandler;
import servent.handler.PutHandler;
import servent.handler.SorryHandler;
import servent.handler.TellGetHandler;
import servent.handler.UpdateHandler;
import servent.handler.WelcomeHandler;
import servent.handler.project.*;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	private CLIParser parser;
	
	private SuzukiKasamiMutex mutex;
	
	public SimpleServentListener(SuzukiKasamiMutex suzukiKasamiMutex) {
		mutex = suzukiKasamiMutex;
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
					case PUSH_RESOLVE_TELL:
						messageHandler = new PushResolveTellHandler(clientMessage);
						break;
					case PUSH_RESOLVE:
						messageHandler = new PushResolveHandler(clientMessage);
						break;
					case COMMIT_TELL:
						messageHandler = new CommitTellHandler(clientMessage, mutex);
						break;
					case REMOVE_FILE:
						messageHandler = new RemoveFileHandler(clientMessage);
						break;
					case REMOVE_FILE_TELL:
						messageHandler = new RemoveFileTellHandler(clientMessage, mutex);
						break;
					case ADD_TELL:
						messageHandler = new AddTellHandler(clientMessage, mutex);
						break;
					case PULL:
						messageHandler = new PullHandler(clientMessage);
						break;
					case PULL_TELL:
						messageHandler = new PullTellHandler(clientMessage, mutex);
						break;
					case CONFLICT:
						messageHandler = new ConflictHandler(clientMessage,mutex);
						break;
					case COMMIT:
						messageHandler = new CommitHandler(clientMessage);
						break;
					case ADD: 
						messageHandler = new AddHandler(clientMessage);
						break;
					case GOODBYE_TELL :
						messageHandler = new GoodbyeTellHandler(clientMessage, mutex, this, parser);
						break;
					case PUT_TELL:
						messageHandler = new PutTellHandler(clientMessage, mutex);
						break;
					case CONNECTED:
						messageHandler = new ConnectedHandler(clientMessage, mutex);
						break;
					case REQUEST:
						messageHandler = new RequestHandler(clientMessage, mutex);
						break;
					case TOKEN:
						messageHandler = new TokenHandler(clientMessage, mutex);
						break;
					case GOODBYE:
						messageHandler = new GoodbyeHandler(clientMessage, mutex);
						break;
					case REMOVE_UPDATE:
						messageHandler = new RemoveUpdateHandler(clientMessage);
						break;
					case NEW_NODE:
						messageHandler = new NewNodeHandler(clientMessage, mutex);
						break;
					case WELCOME:
						messageHandler = new WelcomeHandler(clientMessage);
						break;
					case SORRY:
						messageHandler = new SorryHandler(clientMessage);
						break;
					case UPDATE:
						messageHandler = new UpdateHandler(clientMessage);
						break;
					case PUT:
						messageHandler = new PutHandler(clientMessage);
						break;
					case ASK_GET:
						messageHandler = new AskGetHandler(clientMessage);
						break;
					case TELL_GET:
						messageHandler = new TellGetHandler(clientMessage, mutex);
						break;
					case POISON:
						break;
				}
				
				threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

	public SuzukiKasamiMutex getMutex() {
		return mutex;
	}
	

	public void setParser(CLIParser parser) {
		this.parser = parser;
	}
	
}
