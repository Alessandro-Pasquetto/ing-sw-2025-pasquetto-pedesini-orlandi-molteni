package org.progetto.client.connection.rmi;

import org.progetto.server.connection.Sender;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualClient extends Remote, Sender {
    @Override
    void sendMessage(Object objMessage) throws RemoteException;

    @Override
    void sendPing() throws RemoteException;
}
