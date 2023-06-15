package cli.command;

import app.AppConfig;
import app.ChordState;
import mutex.SuzukiKasamiMutex;
import servent.SimpleServentListener;

public class DHTPutCommand implements CLICommand {
	

	private SuzukiKasamiMutex mutex;
	
	public DHTPutCommand(SuzukiKasamiMutex mutex) {
		this.mutex = mutex;
	}

	@Override
	public String commandName() {
		return "dht_put";
	}

	@Override
	public void execute(String args) {
		String[] splitArgs = args.split(" ");
		
		if (splitArgs.length == 2) {
			
			mutex.lock();
			
			int key = 0;
			int value = 0;
			try {
				key = Integer.parseInt(splitArgs[0]);
				value = Integer.parseInt(splitArgs[1]);
				
				if (key < 0 || key >= ChordState.CHORD_SIZE) {
					throw new NumberFormatException();
				}
				if (value < 0) {
					throw new NumberFormatException();
				}
				
				AppConfig.chordState.putValue(key, value, AppConfig.myServentInfo);
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Invalid key and value pair. Both should be ints. 0 <= key <= " + ChordState.CHORD_SIZE
						+ ". 0 <= value.");
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
		}

	}

}
