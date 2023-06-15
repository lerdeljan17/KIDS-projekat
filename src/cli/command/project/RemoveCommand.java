package cli.command.project;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cli.command.CLICommand;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.message.project.RemoveFileMessage;
import servent.message.util.MessageUtil;

import java.util.Optional;

public class RemoveCommand implements CLICommand{

	private SuzukiKasamiMutex mutex;
	
	public RemoveCommand(SuzukiKasamiMutex mutex) {
		this.mutex = mutex;
	}
	
	@Override
	public String commandName() {
		return "remove";
	}
	
	@Override
	public void execute(String args) {
		
		if(args.split(" ").length > 1) {
			AppConfig.timestampedErrorPrint("Invalid args for RemoveCommand!");
			return;
		}
		
		mutex.lock();
		
		String name = args;
		
		int key = ChordState.chordStringHash(name);
		
		int val = AppConfig.chordState.checkValue(key, name);
		

		if (val == -2) {


			ChordState.currentlyWorkingOn.put(name, Optional.empty());
			
			ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
			RemoveFileMessage removeMessage = new RemoveFileMessage(AppConfig.myServentInfo, receiver, key, name, AppConfig.myServentInfo);
			MessageUtil.sendMessage(removeMessage);
			
		} else if (val == -1) {

			AppConfig.timestampedStandardPrint("No such combination  key = " + key + " value = " + name);
			
			mutex.unlock(false);
		} else {
			

			MyFile myFile = AppConfig.chordState.remove(key, name);
			if(myFile == null) {

				if(ChordState.currentlyWorkingOn.isEmpty()) {
					mutex.unlock(false);
				}		
			} else {
				

				String parts[] = myFile.getContent().split(",");
				
				for(String s : parts) {
					key = ChordState.chordStringHash(s);
					
					ChordState.currentlyWorkingOn.put(s, Optional.empty());
					
					ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
					
					if(AppConfig.chordState.isKeyMine(key)) {
						receiver = AppConfig.myServentInfo;
					}
					
					RemoveFileMessage newMessage = new RemoveFileMessage(AppConfig.myServentInfo, receiver, key, s, AppConfig.myServentInfo);
					MessageUtil.sendMessage(newMessage);
				}
				
			}
			
		}
		
	}
	
}
