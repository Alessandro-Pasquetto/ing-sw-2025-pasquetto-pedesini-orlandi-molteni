package org.progetto.client.connection.socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


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
        this.setName("SocketWriterThread");
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
            }
        }
    }

    /**
     * Stops the writer thread and closes the output stream
     *
     * @author Alessandro
     */
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

    /**
     * Sends a message object to the output stream
     *
     * @author Alessandro
     * @param messageObj the message object to send
     */
    public static void sendMessage(Object messageObj) {
        try {
            messageQueue.put(messageObj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}