package org.progetto.server.connection;

import java.rmi.RemoteException;

public interface Sender {
    void sendMessage(Object messageObj) throws RemoteException;

    void sendPing() throws RemoteException;
}