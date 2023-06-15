package cli.command.project;

import app.AppConfig;
import app.ChordState;
import cli.command.CLICommand;
import model.MyFile;

public class ViewCommand implements CLICommand{

	@Override
	public String commandName() {
		return "view";
	}


	/**
	 * Pravi fajl u working directory sa file name + _tmp i ponovo ispisuje da se napravi izbor
	 * @param args
	 */
	@Override
	public void execute(String args) {

		ChordState.currentData.getStorageVersion().setName(ChordState.currentFileName.concat("_tmp"));
		MyFile.myFileToWorkingFile(ChordState.currentData.getStorageVersion());
//		System.out.println("Conflict happened! with file "+ ChordState.currentFileName + " Please choose action command.\n1. view\n2. push\n3. pull\n");
		AppConfig.timestampedStandardPrint("Conflict happened! with file "+ ChordState.currentFileName.split("_")[0] + " Please choose action command. 1. view 2. push 3. pull");

		
	}

}
