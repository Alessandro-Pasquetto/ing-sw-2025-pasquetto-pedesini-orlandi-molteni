package org.progetto.client.connection.socket;

import javafx.application.Platform;
import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.gui.PageController;
import org.progetto.client.model.GameData;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;

/**
 * Socket message listener for messages coming from server
 */
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

        try {
            while (running) {
                Object messageObj = in.readObject();
                messageDispatcher(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {

            if(e instanceof SocketException){
                System.err.println("SocketServer unreachable");

                close();
            }
            else
                e.printStackTrace();
        }
    }

    public static void setIsHandling(boolean isHandling) {
        SocketListener.isHandling = isHandling;
    }

    // It allows executing an handleMessage one at a time, except when a thread goes into wait inside the handleMessage method
    public static void messageDispatcher(Object messageObj) {

        new Thread(() -> {
            waitHandler();
            processMessage(messageObj);
            isHandling = false;
            notifyHandler();
        }).start();
    }

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

    private static void notifyHandler() {
        synchronized (handlerMessageLock) {
            handlerMessageLock.notify();
        }
    }

    private static void processMessage(Object objMessage) {

        if (GameData.getUIType().equals("GUI")) {
            Platform.runLater(() -> {
                GuiHandlerMessage.handleMessage(objMessage);
            });
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

    public void close() {
        SocketListener.stopListener();
        SocketWriter.stopWriter();

        System.out.println("You have disconnected!");
        if(GameData.getUIType().equals("GUI")) {
            Platform.runLater(() -> {
                try {
                    PageController.switchScene("connection.fxml", "Connection");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}