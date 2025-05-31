package org.progetto.server.connection;

public class MessageSenderService {

    // Used to make the gameThread wait only if the request is sent (it doesn't throw an exception)
    public static void sendCritical(Object messageObj, Sender sender) throws Exception {
        try {
            sender.sendMessage(messageObj);
        }catch(Exception e) {
            System.err.println("Client unreachable");
            e.printStackTrace();
            throw e;
        }
    }

    // Used in cases where the actual sending of the message is not required
    public static void sendOptional(Object messageObj, Sender sender) {
        try {
            sender.sendMessage(messageObj);
        } catch (Exception _) {
        }
    }
}