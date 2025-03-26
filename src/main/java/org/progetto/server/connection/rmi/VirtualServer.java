package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {
    void connect(VirtualView view) throws RemoteException;
}