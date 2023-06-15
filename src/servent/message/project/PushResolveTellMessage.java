package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PushResolveTellMessage extends BasicMessage {

    private ServentInfo whoDidPush;
    private MyFile myFile;
    private String oldName;

    public PushResolveTellMessage(ServentInfo sender, ServentInfo receiver, MyFile myFile, ServentInfo whoDidPush, String oldName) {
        super(MessageType.PUSH_RESOLVE_TELL, sender, receiver);
        this.whoDidPush = whoDidPush;
        this.myFile = myFile;
        this.oldName = oldName;
    }

    public ServentInfo getWhoDidPush() {
        return whoDidPush;
    }

    public MyFile getData() {
        return myFile;
    }

    public void setWhoDidPush(ServentInfo whoDidPush) {
        this.whoDidPush = whoDidPush;
    }

    public String getOldName() {
        return oldName;
    }
}
