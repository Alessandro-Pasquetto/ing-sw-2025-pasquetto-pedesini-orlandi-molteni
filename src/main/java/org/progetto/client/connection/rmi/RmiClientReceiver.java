package org.progetto.client.connection.rmi;

import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.TuiCommandFilter;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiClientReceiver extends UnicastRemoteObject implements VirtualClient {

    private final static int NUM_DISPATCHER_THREADS = 2;

    private static RmiClientReceiver instance = null;
    private static final LinkedBlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();

    private static final ExecutorService dispatcherPool = Executors.newFixedThreadPool(NUM_DISPATCHER_THREADS);

    protected RmiClientReceiver() throws RemoteException {
        super();
        messageDispatcher();
    }

    public static RmiClientReceiver getInstance() throws RemoteException {
        if (instance == null)
            instance = new RmiClientReceiver();

        return instance;
    }

    public void messageDispatcher() {

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

        dispatcherPool.execute(() -> {
            while (true) {
                try {
                    if (TuiCommandFilter.getIsWaitingResponse()) {
                        Object message = messageQueue.take();
                        processMessage(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

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