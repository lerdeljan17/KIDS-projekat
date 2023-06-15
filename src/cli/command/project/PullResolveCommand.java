package cli.command.project;

import app.AppConfig;
import app.ChordState;
import cli.command.CLICommand;
import model.MyFile;

import java.io.File;

public class PullResolveCommand implements CLICommand {
    @Override
    public String commandName() {
        return "pull_resolve";
    }

    @Override
    public void execute(String args) {

        // TODO: 4.6.2021. msm da ce ovako da bas prepise preko postojeceg fajla
        File f = new File(AppConfig.myServentInfo.getWorkingDirectory() + File.separator + ChordState.currentData.getMyVersion().getName());
//        AppConfig.timestampedErrorPrint("pull_resolve: deleting file " + f.getPath() );
        f.delete();
        ChordState.currentData.getMyVersion().setVersion(ChordState.currentData.getStorageVersion().getVersion());
        ChordState.currentData.getMyVersion().setContent(ChordState.currentData.getStorageVersion().getContent());

        MyFile.myFileToWorkingFile(ChordState.currentData.getMyVersion());
        AppConfig.timestampedStandardPrint("pull_resolve: myVersion: " + ChordState.currentData.getMyVersion());
        ChordState.currentData = null;
        ChordState.currentFileName = "";
        ChordState.currentlyResolvingConflict.set(false);
    }
}
