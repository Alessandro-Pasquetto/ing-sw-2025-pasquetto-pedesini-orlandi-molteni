package org.progetto.server.connection.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;


public class RmiServer extends Thread {

    public RmiServer() {
        this.setName("RmiServerThread");
    }

    @Override
    public void run() {
        try {
            VirtualServer rmiClientHandler = new RmiServerReceiver();
            LocateRegistry.createRegistry(1099);
            Naming.rebind("VirtualServer", rmiClientHandler);

            System.out.println("RMIServer listening on port 1099...");
        } catch (Exception e) {
            System.out.println("Error RMI server: " + e.getMessage());
        }
    }
}