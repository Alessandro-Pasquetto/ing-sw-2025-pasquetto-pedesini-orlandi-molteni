package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualView;
import org.progetto.server.controller.GameManager;
import org.progetto.server.model.Player;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;

public class RmiServer extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final ArrayList<VirtualView> rmiClients = new ArrayList<>();
    private final static HashMap<VirtualView, GameManager> virtualViewGameManager = new HashMap<>();

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

    public static void addRmiClient(VirtualView rmiClient) {
        synchronized (rmiClients) {
            rmiClients.add(rmiClient);
        }
    }

    public static void removeRmiClient(VirtualView rmiClient) {
        synchronized (rmiClients) {
            rmiClients.remove(rmiClient);
        }
    }

    public static void addVirtualViewGameManager(VirtualView rmiClient, GameManager gameManager) {
        synchronized (virtualViewGameManager) {
            virtualViewGameManager.put(rmiClient, gameManager);
        }
    }

    public static GameManager getVirtualViewGameManager(VirtualView rmiClient) {
        synchronized (virtualViewGameManager) {
            return virtualViewGameManager.get(rmiClient);
        }
    }

    public static void broadcastMessage(Object messageObj) {

        ArrayList<VirtualView> rmiClientsCopy;

        synchronized (rmiClients) {
            rmiClientsCopy = new ArrayList<>(rmiClients);
        }

        try{
            for (VirtualView vv : rmiClientsCopy) {
                vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcastMessageToOthers(VirtualView sender, Object messageObj) {

        ArrayList<VirtualView> rmiClientsCopy;

        synchronized (rmiClients) {
            rmiClientsCopy = new ArrayList<>(rmiClients);
        }

        try{
            for (VirtualView vv : rmiClientsCopy) {
                if(!vv.equals(sender))
                    vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}