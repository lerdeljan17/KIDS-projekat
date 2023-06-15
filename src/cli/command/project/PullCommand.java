package cli.command.project;

import java.util.Arrays;
import java.util.Optional;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cli.command.CLICommand;
import mutex.SuzukiKasamiMutex;
import servent.message.project.PullMessage;
import servent.message.util.MessageUtil;

public class PullCommand implements CLICommand{
	
	private SuzukiKasamiMutex mutex;
	
	public PullCommand(SuzukiKasamiMutex mutex) {
		this.mutex = mutex;
	}

	@Override
	public String commandName() {
		return "pull";
	}

	@Override
	public void execute(String args) {
		
		try {
			
			int version = -1;
			
			String parts[] = args.split(" ");
			if(parts.length > 1) {
				try {
					version = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
					return;
				}
			}
			
			mutex.lock();
			
			String name = parts[0];
			int key = ChordState.chordStringHash(name);
			
			
			int val = AppConfig.chordState.checkValue(key, name);
			

			if (val == -2) {

				AppConfig.timestampedStandardPrint("Please wait...");
				
				ChordState.currentlyWorkingOn.put(name, Optional.empty());
				
				ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
				PullMessage pullMessage = new PullMessage(AppConfig.myServentInfo, receiver, key, name, version, AppConfig.myServentInfo);
				MessageUtil.sendMessage(pullMessage);
				
			} else if (val == -1) {

				AppConfig.timestampedStandardPrint("Does not exist  key = " + key + " value = " + name);
				
				mutex.unlock(false);
			} else {
				
				ChordState.currentlyWorkingOn.put(name, Optional.empty());
				
				PullMessage pullMessage = new PullMessage(AppConfig.myServentInfo, AppConfig.myServentInfo, key, name, version, AppConfig.myServentInfo);
				MessageUtil.sendMessage(pullMessage);
				
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint( e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
		}
		
	}

}
