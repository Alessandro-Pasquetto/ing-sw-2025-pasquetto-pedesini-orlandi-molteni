package org.progetto.client.connection.rmi;

import org.progetto.server.connection.Sender;

import java.rmi.Remote;
import java.rmi.RemoteException;

// The VirtualClient must be a sender on the server side
public interface VirtualClient extends Remote, Sender {

    void sendMessage(Object objMessage) throws RemoteException;

    void ping() throws RemoteException;
}
