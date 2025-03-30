package org.progetto.client.connection.socket;

import org.progetto.client.connection.HandlerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Socket message listener for messages coming from server
 */
public class SocketListener extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private static ObjectInputStream in;
    private static boolean running = true;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SocketListener(ObjectInputStream in) {
        this.in = in;
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void run() {
        try {
            while (running) {
                Object messageObj = in.readObject();
                HandlerMessage.handleMessage(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            stopListener();
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