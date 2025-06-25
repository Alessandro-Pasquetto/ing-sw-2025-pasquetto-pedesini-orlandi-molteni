package org.progetto.client.connection.socket;

import javafx.application.Platform;
import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.gui.PageController;
import org.progetto.client.model.GameData;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;


public class SocketListener extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private static ObjectInputStream in;
    private static boolean running = true;

    private static final Object handlerMessageLock = new Object();
    private static boolean isHandling = false;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SocketListener(ObjectInputStream in) {
        SocketListener.in = in;
        this.setName("SocketListenerThread");
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public void run() {
        while (running) {
            try {
                Object messageObj = in.readObject();
                messageDispatcher(messageObj);

            } catch (IOException | ClassNotFoundException e) {

                if(e instanceof SocketException){
                    System.err.println("SocketServer unreachable");
                }
                else
                    e.printStackTrace();
            }
        }
    }

    /**
     * Sets the isHandling attribute to true or false
     *
     * @author Alessandro
     * @param isHandling true if a message is being handled, false otherwise
     */
    public static void setIsHandling(boolean isHandling) {
        SocketListener.isHandling = isHandling;
    }

    /**
     * It allows executing an handleMessage one at a time, except when a thread goes into wait inside the handleMessage method
     *
     * @author Alessandro
     * @param messageObj the message object to be processed
     */
    public static void messageDispatcher(Object messageObj) {
        new Thread(() -> {
            waitHandler();
            processMessage(messageObj);
            isHandling = false;
            notifyHandler();
        }).start();
    }

    /**
     * Waits for the handler to be available
     *
     * @author Alessandro
     */
    private static void waitHandler(){
        synchronized (handlerMessageLock) {
            try {
                while (isHandling)
                    handlerMessageLock.wait();

                isHandling = true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Notifies the handler that a message has been processed
     *
     * @author Alessandro
     */
    private static void notifyHandler() {
        synchronized (handlerMessageLock) {
            handlerMessageLock.notify();
        }
    }

    /**
     * Processes the message based on the UI type
     *
     * @author Alessandro
     * @param objMessage the message object to be processed
     */
    private static void processMessage(Object objMessage) {

        if (GameData.getUIType().equals("GUI")) {
            Platform.runLater(() -> {
                GuiHandlerMessage.handleMessage(objMessage);
            });
        } else if (GameData.getUIType().equals("TUI")) {
            TuiHandlerMessage.handleMessage(objMessage);
        }
    }
}