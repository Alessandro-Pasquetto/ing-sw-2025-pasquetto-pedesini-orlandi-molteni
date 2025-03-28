package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualView;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class RmiServer extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final ArrayList<VirtualView> lobbyRmiClients = new ArrayList<>();

    // =======================
    // MAIN
    // =======================

    @Override
    public void run() {
        try {
            VirtualServer rmiClientHandler = new RmiServerReceiver();
            LocateRegistry.createRegistry(1099);
            Naming.rebind("VirtualServer", rmiClientHandler);

            System.out.println("RMIServer listening on port 1099...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =======================
    // OTHER FUNCTIONS
    // =======================

    public static void addLobbyRmiClient(VirtualView rmiClient) {
        synchronized (lobbyRmiClients) {
            lobbyRmiClients.add(rmiClient);
        }
    }

    public static void removeLobbyRmiClient(VirtualView rmiClient) {
        synchronized (lobbyRmiClients) {
            lobbyRmiClients.remove(rmiClient);
        }
    }

    public static void broadcastLobbyMessage(Object messageObj) {

        ArrayList<VirtualView> lobbyRmiClientsCopy;

        synchronized (lobbyRmiClients) {
            lobbyRmiClientsCopy = new ArrayList<>(lobbyRmiClients);
        }

        try{
            for (VirtualView vv : lobbyRmiClientsCopy) {
                vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcastLobbyMessageToOthers(VirtualView sender, Object messageObj) {

        ArrayList<VirtualView> lobbyRmiClientsCopy;

        synchronized (lobbyRmiClients) {
            lobbyRmiClientsCopy = new ArrayList<>(lobbyRmiClients);
        }

        try{
            for (VirtualView vv : lobbyRmiClientsCopy) {
                if(!vv.equals(sender))
                    vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}