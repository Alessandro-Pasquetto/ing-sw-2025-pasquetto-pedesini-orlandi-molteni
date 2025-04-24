package org.progetto.client.connection.rmi;

import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.model.GameData;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiClientReceiver extends UnicastRemoteObject implements VirtualClient {

    private static RmiClientReceiver instance = null;
    private static final LinkedBlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();

    protected RmiClientReceiver() throws RemoteException {
        super();
        MessageDispatcher();
    }

    public static RmiClientReceiver getInstance() throws RemoteException {
        if (instance == null)
            instance = new RmiClientReceiver();

        return instance;
    }

    public void MessageDispatcher() {
        new Thread(() -> {
            while (true) {
                try {
                    Object message = messageQueue.take();
                    processMessage(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void processMessage(Object objMessage) {
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
        messageQueue.offer(objMessage);
    }
}