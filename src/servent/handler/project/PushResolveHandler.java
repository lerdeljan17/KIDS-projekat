package servent.handler.project;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.project.PushResolveMessage;

import java.util.Arrays;

public class PushResolveHandler implements MessageHandler {

    private Message clientMessage;

    public PushResolveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {

            if(MessageType.PUSH_RESOLVE == clientMessage.getMessageType()) {

                PushResolveMessage message = (PushResolveMessage) clientMessage;

                AppConfig.chordState.pushFile(message.getKey(), message.getData(), message.getOriginalSender(),message.getOldName());


            } else {
                AppConfig.timestampedErrorPrint("PushResolveHandler got a message that is not PushResolve");
            }

        } catch (Exception e) {
            AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
        }

    }
    }

