package org.progetto.server.connection;

public class MessageSenderService {

    // Used in cases where the actual sending of the message is not required
    public static void sendMessage(Object messageObj, Sender sender) {
        try {
            sender.sendMessage(messageObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}