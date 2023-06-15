package cli.command.project;

import java.io.File;
import java.util.Optional;

import app.AppConfig;
import app.ChordState;
import cli.command.CLICommand;
import model.MyFile;
import mutex.SuzukiKasamiMutex;

public class AddCommand implements CLICommand{
	
	private SuzukiKasamiMutex mutex;
	
	public AddCommand(SuzukiKasamiMutex mutex) {
		this.mutex = mutex;
	}

	@Override
	public String commandName() {
		return "add";
	}

	@Override
	public void execute(String args) {
		
		mutex.lock();
		

		String fileName = AppConfig.myServentInfo.getWorkingDirectory() + File.separator + args;
		

		File file = new File(fileName);
		
		if(!file.exists()) {
			AppConfig.timestampedErrorPrint("File with path " + fileName + " does not exists!");
			return;
		}
		
		fileRec(file);
			
	}

	private void fileRec(File file) {
		
		MyFile value = MyFile.workingFileToMyFile(file, AppConfig.myServentInfo.getWorkingDirectory());
		
		ChordState.currentlyWorkingOn.putIfAbsent(value.getName(), Optional.empty());
		
		int key = ChordState.chordStringHash(value.getName());
		
		AppConfig.chordState.addFile(key, value, AppConfig.myServentInfo);
		
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				fileRec(f);
			}
		}
		
	}

}
