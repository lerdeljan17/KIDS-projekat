package servent.handler.project;

import java.util.Arrays;
import java.util.Optional;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.AddTellMessage;
import servent.message.util.MessageUtil;

public class AddTellHandler implements MessageHandler{
	
	private SuzukiKasamiMutex mutex;
	private Message clientMessage;
	
	public AddTellHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
		super();
		this.mutex = mutex;
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {

		try {

			if(MessageType.ADD_TELL == clientMessage.getMessageType()) {
				
				AddTellMessage message = (AddTellMessage) clientMessage;
				
				if(message.getOriginalSender().getChordId() == AppConfig.myServentInfo.getChordId()) {
					
					MyFile myFile = message.getData();
					ChordState.currentlyWorkingOn.put(myFile.getName().split("_")[0], Optional.ofNullable(myFile));

					MyFile.changeFileName(myFile.getName());
					
					if(AppConfig.chordState.isMapCompleted()) {
						ChordState.currentlyWorkingOn.clear();
						mutex.unlock(false);
					}					
					
				} else {
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());	
					AddTellMessage newMessage = new AddTellMessage(AppConfig.myServentInfo, receiver, message.getData(), message.getOriginalSender());
					MessageUtil.sendMessage(newMessage);
				}
				
			} else {
				AppConfig.timestampedErrorPrint("AddTell handler got a message that is not ADD_TELL");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Exception in AddTellHandler. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
