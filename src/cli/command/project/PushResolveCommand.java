package cli.command.project;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cli.command.CLICommand;
import model.MyFile;
import servent.message.project.PushResolveMessage;
import servent.message.util.MessageUtil;

public class PushResolveCommand implements CLICommand {
    @Override
    public String commandName() {
        return "push_resolve";
    }

    @Override
    public void execute(String args) {
        String oldName =  ChordState.currentData.getMyVersion().getName();
        ChordState.currentData.getMyVersion().setVersion(ChordState.currentData.getStorageVersion().getVersion());
        // salje neku push resolve poruku koja ce da prepise fajl i da vrati push tell resolve npr. i da obori flag
        MyFile myFile = ChordState.currentData.getMyVersion();
        int key = ChordState.chordStringHash(ChordState.currentFileName.split("_")[0]);
        ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(key);
        PushResolveMessage pushResolveMessage = new PushResolveMessage(AppConfig.myServentInfo,receiver,key, myFile,AppConfig.myServentInfo,oldName);
        MessageUtil.sendMessage(pushResolveMessage);
    }
}
