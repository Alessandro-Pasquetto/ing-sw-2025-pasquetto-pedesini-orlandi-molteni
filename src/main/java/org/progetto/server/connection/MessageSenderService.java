package org.progetto.server.connection;

public class MessageSenderService {

    public static void sendCritical(Object messageObj, Sender sender) throws Exception {
        try {
            sender.sendMessage(messageObj);
        }catch(Exception e) {
            System.err.println("Client unreachable");
            throw e;
        }
    }

    public static void sendOptional(Object messageObj, Sender sender) {
        try {
            sender.sendMessage(messageObj);
        } catch (Exception e) {
            System.err.println("Client unreachable");
        }
    }
}