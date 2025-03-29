package org.progetto.client.connection.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualClient extends Remote {
    void sendMessage(Object objMessage) throws RemoteException;
}
