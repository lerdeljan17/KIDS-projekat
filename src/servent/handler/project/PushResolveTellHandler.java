package servent.handler.project;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import model.MyFile;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.PushResolveTellMessage;
import servent.message.util.MessageUtil;

import java.io.File;
import java.util.Arrays;

public class PushResolveTellHandler implements MessageHandler {

    private Message clientMessage;

    public PushResolveTellHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {

            if(MessageType.PUSH_RESOLVE_TELL == clientMessage.getMessageType()) {

                PushResolveTellMessage message = (PushResolveTellMessage) clientMessage;

              if (AppConfig.myServentInfo.getChordId() == message.getWhoDidPush().getChordId()) {
                    if (ChordState.currentFileName.equals(message.getData().getName().split("_")[0])) {

                        File f = new File(AppConfig.myServentInfo.getWorkingDirectory() + File.separator + message.getOldName());
//        AppConfig.timestampedErrorPrint("pull_resolve: deleting file " + f.getPath() );
                        f.delete();
                        MyFile.myFileToWorkingFile(message.getData());
                        ChordState.currentData = null;
                        ChordState.currentFileName = "";
                        ChordState.currentlyResolvingConflict.set(false);


                    }
                }else {
                  ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getWhoDidPush().getChordId());
                  PushResolveTellMessage newMessage = new PushResolveTellMessage(AppConfig.myServentInfo, receiver, message.getData(), message.getWhoDidPush(), message.getOldName());
                  MessageUtil.sendMessage(newMessage);
              }


            } else {
                AppConfig.timestampedErrorPrint("PushResolveTellHandler got a message that is not PushResolveTellMessage");
            }

        } catch (Exception e) {
            AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
        }
    }
}
