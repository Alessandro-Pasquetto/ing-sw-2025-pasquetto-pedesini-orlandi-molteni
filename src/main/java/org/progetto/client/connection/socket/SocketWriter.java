package org.progetto.client.connection.socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketWriter extends Thread {

    private static ObjectOutputStream out = null;
    private static BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private static boolean running = true;

    public SocketWriter(ObjectOutputStream out) {
        this.out = out;
        this.messageQueue = new LinkedBlockingQueue<>();
    }

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