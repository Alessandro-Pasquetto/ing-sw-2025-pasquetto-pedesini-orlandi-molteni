package org.progetto.client.connection.rmi;

import org.progetto.client.connection.HandlerMessage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClientReceiver extends UnicastRemoteObject implements VirtualView {

    private static RmiClientReceiver instance = null;

    protected RmiClientReceiver() throws RemoteException {
        super();
    }

    public static RmiClientReceiver getInstance() throws RemoteException {
        if (instance == null)
            instance = new RmiClientReceiver();

        return instance;
    }

    @Override
    public void sendMessage(Object objMessage) throws RemoteException {
        HandlerMessage.handleMessage(objMessage);
    }
}