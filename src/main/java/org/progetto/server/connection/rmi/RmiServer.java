package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualView;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class RmiServer extends UnicastRemoteObject implements VirtualServer {

    final static List<VirtualView> clients = new ArrayList<>();

    protected RmiServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            VirtualServer server = new RmiServer();
            LocateRegistry.createRegistry(1099);
            Naming.rebind("VirtualServer", server);

            System.out.println("RMIServer listening on port 1099...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(VirtualView client) throws RemoteException {
        synchronized (clients) {
            clients.add(client);
        }

        ciao();
    }


    public static void ciao() throws RemoteException {

        synchronized (clients) {
            for (VirtualView client : clients) {
                client.showCiao("ciao");
            }
        }
    }
}