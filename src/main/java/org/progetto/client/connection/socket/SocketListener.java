package org.progetto.client.connection.socket;

import org.progetto.client.PageController;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.messages.toClient.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class SocketListener extends Thread {

    private static ObjectInputStream in;
    private static boolean running = true;

    public SocketListener(ObjectInputStream in) {
        this.in = in;
    }

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