package org.progetto.server.connection;

public class MessageSenderService {

    /**
     * Sends a message object using the provided sender
     *
     * @author Alessandro
     * @param messageObj The message object to be sent.
     * @param sender The sender responsible for sending the message.
     */
    public static void sendMessage(Object messageObj, Sender sender) {
        new Thread(() -> {
            try {
                sender.sendMessage(messageObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}