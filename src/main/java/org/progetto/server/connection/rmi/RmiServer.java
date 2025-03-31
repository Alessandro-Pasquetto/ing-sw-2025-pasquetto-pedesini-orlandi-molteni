package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

/**
 * Handles RMI clients connection and messages
 */
public class RmiServer extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final ArrayList<VirtualClient> lobbyRmiClients = new ArrayList<>();

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
            System.out.println("Error RMI server: " + e.getMessage());
        }
    }

    // =======================
    // OTHER FUNCTIONS
    // =======================

    // Methods to set and communicate with the rmiClients in the lobby

    public static void addLobbyRmiClient(VirtualClient rmiClient) {
        synchronized (lobbyRmiClients) {
            lobbyRmiClients.add(rmiClient);
        }
    }

    public static void removeLobbyRmiClient(VirtualClient rmiClient) {
        synchronized (lobbyRmiClients) {
            lobbyRmiClients.remove(rmiClient);
        }
    }

    public static void broadcastLobbyMessage(Object messageObj) {

        ArrayList<VirtualClient> lobbyRmiClientsCopy;

        synchronized (lobbyRmiClients) {
            lobbyRmiClientsCopy = new ArrayList<>(lobbyRmiClients);
        }

        try{
            for (VirtualClient vv : lobbyRmiClientsCopy) {
                vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcastLobbyMessageToOthers(Sender sender, Object messageObj) {

        ArrayList<VirtualClient> lobbyRmiClientsCopy;

        synchronized (lobbyRmiClients) {
            lobbyRmiClientsCopy = new ArrayList<>(lobbyRmiClients);
        }

        try{
            for (VirtualClient vv : lobbyRmiClientsCopy) {
                if(!vv.equals(sender))
                    vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}