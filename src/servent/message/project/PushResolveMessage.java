package servent.message.project;

import app.ServentInfo;
import model.MyFile;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PushResolveMessage extends BasicMessage {

    private ServentInfo originalSender;
    private MyFile myFile;
    private int key;
    private String oldName;

    public PushResolveMessage(ServentInfo sender, ServentInfo receiver, int key, MyFile myFile, ServentInfo originalSender, String oldName) {
        super(MessageType.PUSH_RESOLVE, sender, receiver);
        this.originalSender = originalSender;
        this.key = key;
        this.myFile = myFile;
        this.oldName = oldName;
    }

    public int getKey() {
        return key;
    }

    public ServentInfo getOriginalSender() {
        return originalSender;
    }

    public MyFile getData() {
        return myFile;
    }

    public String getOldName() {
        return oldName;
    }
}
