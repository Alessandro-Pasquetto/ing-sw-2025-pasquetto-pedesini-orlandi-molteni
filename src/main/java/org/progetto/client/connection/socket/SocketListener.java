package org.progetto.client.connection.socket;

import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.model.GameData;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket message listener for messages coming from server
 */
public class SocketListener extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private final static int NUM_DISPATCHER_THREADS = 2;

    private static ObjectInputStream in;
    private static boolean running = true;

    private static final LinkedBlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();

    private static final ExecutorService dispatcherPool = Executors.newFixedThreadPool(NUM_DISPATCHER_THREADS);

    // =======================
    // CONSTRUCTORS
    // =======================

    public SocketListener(ObjectInputStream in) {
        SocketListener.in = in;
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void run() {

        messageDispatcher();

        try {
            while (running) {
                Object messageObj = in.readObject();
                messageQueue.offer(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            stopListener();
        }
    }

    public static void messageDispatcher() {

        for (int i = 0; i < NUM_DISPATCHER_THREADS; i++) {
            dispatcherPool.execute(() -> {
                while (true) {
                    try {
                        Object message = messageQueue.take();
                        processMessage(message);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    private static void processMessage(Object objMessage) {

        if (GameData.getUIType().equals("GUI")) {
            GuiHandlerMessage.handleMessage(objMessage);
        } else if (GameData.getUIType().equals("TUI")) {
            TuiHandlerMessage.handleMessage(objMessage);
        }
    }

    public static void stopListener() {
        running = false;
        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}