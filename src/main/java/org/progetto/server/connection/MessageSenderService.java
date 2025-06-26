package org.progetto.server.connection;

import java.util.concurrent.*;

public class MessageSenderService {

    private static final long SEND_TIMEOUT_MILLIS = 500;

    /**
     * Sends a message object using the provided sender with a timeout of 500 milliseconds
     *
     * @author Alessandro
     * @param messageObj The message object to be sent.
     * @param sender The sender responsible for sending the message.
     */
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static synchronized void sendMessage(Object messageObj, Sender sender) {
        Future<?> future = executor.submit(() -> {
            try {
                sender.sendMessage(messageObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            future.get(SEND_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}