package cli.command.project;

import java.io.File;
import java.util.Optional;

import app.AppConfig;
import app.ChordState;
import cli.command.CLICommand;
import model.MyFile;
import mutex.SuzukiKasamiMutex;

public class CommitCommand implements CLICommand {

	private SuzukiKasamiMutex mutex;

	public CommitCommand(SuzukiKasamiMutex mutex) {
		this.mutex = mutex;
	}

	@Override
	public String commandName() {
		return "commit";
	}

	@Override
	public void execute(String args) {
		mutex.lock();

		String fileName = AppConfig.myServentInfo.getWorkingDirectory() + File.separator + args;

		int key = ChordState.chordStringHash(args);

		File file = new File(fileName);

		if(!file.exists()) {
			AppConfig.timestampedErrorPrint("File with path " + fileName + " does not exists!");
			return;
		}

		fileRec(file);

//		Data value = UtilsData.transformRealFileToData(file,AppConfig.myServentInfo.getWorkingDirectory());

		//	Data value = UtilsData.transformToData(null, file);

//		AppConfig.chordState.updateFile(key, value, AppConfig.myServentInfo);

	}


	private void fileRec(File file) {


		// TODO: 4.6.2021. msm da ako je dir da ne mora da se salje komit, nego samo za fajlove
		if (!file.isDirectory()){
			MyFile value = MyFile.workingFileToMyFile(file, AppConfig.myServentInfo.getWorkingDirectory());
			value.setVersion(Integer.parseInt(value.getName().split("_")[1]));
			// TODO: 4.6.2021. da li ovde treba da se u mapu upise sa ili bez verzije
			ChordState.currentlyWorkingOnForCommit.putIfAbsent(value.getName().split("_")[0], Optional.empty());

			int key = ChordState.chordStringHash(value.getName().split("_")[0]);

			AppConfig.chordState.updateFile(key, value, AppConfig.myServentInfo);
		}

		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				fileRec(f);
			}
		}

	}

}
