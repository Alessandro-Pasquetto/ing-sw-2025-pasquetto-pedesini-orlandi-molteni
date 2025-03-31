package org.progetto.client.connection.rmi;

import org.progetto.server.connection.Sender;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualClient extends Remote, Sender {
    void sendMessage(Object objMessage) throws RemoteException;
}
