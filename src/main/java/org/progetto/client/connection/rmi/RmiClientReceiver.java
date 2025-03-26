package org.progetto.client.connection.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClientReceiver extends UnicastRemoteObject implements VirtualView {

    protected RmiClientReceiver() throws RemoteException {
        super();
    }

    @Override
    public void showCiao(String message) throws RemoteException {
        System.out.println(message);
    }
}