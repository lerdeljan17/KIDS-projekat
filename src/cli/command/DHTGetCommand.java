package cli.command;

import app.AppConfig;
import mutex.SuzukiKasamiMutex;

public class DHTGetCommand implements CLICommand {
	
	private SuzukiKasamiMutex mutex;
	
	public DHTGetCommand(SuzukiKasamiMutex mutex) {
		this.mutex = mutex;
	}

	@Override
	public String commandName() {
		return "dht_get";
	}

	@Override
	public void execute(String args) {
		
		try {
		
			mutex.lock();
			
			int key = Integer.parseInt(args);
			
			int val = AppConfig.chordState.getValue(key);
			
			if (val == -2) {
				AppConfig.timestampedStandardPrint("Please wait...");
			} else if (val == -1) {
				AppConfig.timestampedStandardPrint("No such key: " + key);
				
				 mutex.unlock(false);
			} else {
				AppConfig.timestampedStandardPrint(key + ": " + val);
				
				mutex.unlock(false);
			}
			
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Invalid argument for dht_get: " + args + ". Should be key, which is an int.");
		}
	}

}
