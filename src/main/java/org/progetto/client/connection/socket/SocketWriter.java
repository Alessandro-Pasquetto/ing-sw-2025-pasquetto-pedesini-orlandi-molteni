package org.progetto.client.connection.socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket message writer that sends messages to server
 */
public class SocketWriter extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private static ObjectOutputStream out = null;
    private static LinkedBlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private static boolean running = true;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SocketWriter(ObjectOutputStream out) {
        SocketWriter.out = out;
        messageQueue = new LinkedBlockingQueue<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void run() {
        while (running) {
            try {
                Object messageObj = messageQueue.take();

                out.writeObject(messageObj);
                out.flush();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public static void stopWriter() {
        running = false;
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Object messageObj) {
        try {
            messageQueue.put(messageObj);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}