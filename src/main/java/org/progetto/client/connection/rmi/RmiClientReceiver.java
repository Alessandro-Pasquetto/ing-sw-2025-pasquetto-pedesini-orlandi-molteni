package org.progetto.client.connection.rmi;

import javafx.application.Platform;
import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.model.GameData;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClientReceiver extends UnicastRemoteObject implements VirtualClient {

    private static RmiClientReceiver instance = null;

    private static final Object handlerMessageLock = new Object();
    private static boolean isHandling = false;

    protected RmiClientReceiver() throws RemoteException {
        super();
    }

    /**
     * Returns the single instance of RmiClientReceiver
     *
     * @author Alessandro
     * @return the single instance of RmiClientReceiver
     * @throws RemoteException if there is an error during remote method call
     */
    public static RmiClientReceiver getInstance() throws RemoteException {
        if (instance == null)
            instance = new RmiClientReceiver();

        return instance;
    }

    /**
     * Sets the handling state of the message dispatcher
     *
     * @author Alessandro
     * @param isHandling true if a message is being handled, false otherwise
     */
    public static void setIsHandling(boolean isHandling) {
        RmiClientReceiver.isHandling = isHandling;
    }

    /**
     * Dispatches the message to the appropriate handler
     *
     * @author Alessandro
     * @param messageObj the message object to be processed
     */
    private static void messageDispatcher(Object messageObj) {

        new Thread(() -> {
            waitHandler();
            processMessage(messageObj);
            isHandling = false;
            notifyHandler();
        }).start();
    }

    /**
     * Waits for the handler to be available before processing the message
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
     * Notifies the handler that it can process the next message
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

    /**
     * Method called by the server to notify changes to the client
     *
     * @author Alessandro
     */
    @Override
    public synchronized void sendMessage(Object objMessage) throws RemoteException {
        messageDispatcher(objMessage);
    }

    @Override
    public void ping() throws RemoteException {

    }
}