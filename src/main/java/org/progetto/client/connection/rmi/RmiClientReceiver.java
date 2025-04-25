package org.progetto.client.connection.rmi;

import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.connection.socket.SocketListener;
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

    public static RmiClientReceiver getInstance() throws RemoteException {
        if (instance == null)
            instance = new RmiClientReceiver();

        return instance;
    }

    public static void setIsHandling(boolean isHandling) {
        RmiClientReceiver.isHandling = isHandling;
    }

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
            GuiHandlerMessage.handleMessage(objMessage);
        } else if (GameData.getUIType().equals("TUI")) {
            TuiHandlerMessage.handleMessage(objMessage);
        }
    }

    /**
     * Method called by the server to notify changes to the client
     */
    @Override
    public synchronized void sendMessage(Object objMessage) throws RemoteException {
        messageDispatcher(objMessage);
    }
}