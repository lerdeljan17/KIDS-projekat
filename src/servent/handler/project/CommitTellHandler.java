package servent.handler.project;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import model.MyFile;
import mutex.SuzukiKasamiMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.CommitTellMessage;
import servent.message.util.MessageUtil;

import java.util.Arrays;

public class CommitTellHandler implements MessageHandler {

    private Message clientMessage;
    private SuzukiKasamiMutex mutex;

    public CommitTellHandler(Message clientMessage, SuzukiKasamiMutex mutex) {
        this.clientMessage = clientMessage;
        this.mutex = mutex;
    }


    @Override
    public void run() {

        try {

            if(MessageType.COMMIT_TELL == clientMessage.getMessageType()) {

                CommitTellMessage message = (CommitTellMessage) clientMessage;

                if(message.getOriginalSender().getChordId() == AppConfig.myServentInfo.getChordId()) {

                    MyFile myFile = message.getData();
//                    AppConfig.chordState.currentlyWorkingOn.put(data.getName().split("_")[0], data);
                    AppConfig.chordState.currentlyWorkingOnForCommit.remove(myFile.getName().split("_")[0]);
                    // svi komitovi prosli bez konflikta
                    if(ChordState.checkAndResolveConflicts()) {
                        mutex.unlock(false);
                    }

                } else {

                    ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(message.getOriginalSender().getChordId());
                    CommitTellMessage newMessage = new CommitTellMessage(AppConfig.myServentInfo, receiver, message.getData(), message.getOriginalSender());
                    MessageUtil.sendMessage(newMessage);
                }

            } else {
                AppConfig.timestampedErrorPrint("CommitTell handler got a message that is not COMMIT_TELL");
            }

        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Exception in CommitTell. Message : " + e.getMessage() + "\nError : " + Arrays.toString(e.getStackTrace()));
        }

    }
}
