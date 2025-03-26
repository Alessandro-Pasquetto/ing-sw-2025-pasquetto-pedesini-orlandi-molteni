package org.progetto.client.connection.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualView extends Remote {
    void showCiao(String message) throws RemoteException;
}
