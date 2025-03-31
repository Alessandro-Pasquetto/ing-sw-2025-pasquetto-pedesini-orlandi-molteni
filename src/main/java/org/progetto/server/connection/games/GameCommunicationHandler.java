package org.progetto.server.connection.games;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.TimerController;
import org.progetto.server.model.Game;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * All game communication data to handle multiple clients (Socket/RMI)
 */
public class GameCommunicationHandler {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<SocketWriter> socketWriters = new ArrayList<>();
    private final ArrayList<VirtualClient> rmiClients = new ArrayList<>();
    private final Game game;
    private final TimerController timer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameCommunicationHandler(int idGame, int numPlayers, int level) {
        this.game = new Game(idGame, numPlayers, level);
        this.timer = new TimerController(this::broadcastGameMessage,10,2);
        GameCommunicationHandlerMaps.addWaitingGameManager(idGame, this);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<SocketWriter> getSocketWritersCopy() {
        ArrayList<SocketWriter> socketWritersCopy;

        synchronized (socketWriters) {
            socketWritersCopy = new ArrayList<>(socketWriters);
        }

        return socketWritersCopy;
    }

    public ArrayList<VirtualClient> getRmiClientsCopy() {
        ArrayList<VirtualClient> rmiClientsCopy;

        synchronized (rmiClients) {
            rmiClientsCopy = new ArrayList<>(rmiClients);
        }

        return rmiClientsCopy;
    }

    public Game getGame() {
        return game;
    }

    public TimerController getTimerController() {
        return timer;
    }

    public boolean timerExpired() {
        return timer.getTimerInt() == 0;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void addSocketWriter(SocketWriter socketWriter){
        synchronized (socketWriters){
            socketWriters.add(socketWriter);
        }
    }

    public void removeSocketWriter(SocketWriter socketWriter){
        synchronized (socketWriters ){
            socketWriters.remove(socketWriter);
        }
    }

    public void addRmiClient(VirtualClient rmiClient){
        synchronized (rmiClients){
            rmiClients.add(rmiClient);
        }
    }

    public void removeRmiClient(VirtualClient rmiClient){
        synchronized (rmiClients){
            rmiClients.remove(rmiClient);
        }
    }

    public void broadcastGameMessage(Object messageObj) {
        ArrayList<SocketWriter> socketWritersCopy = getSocketWritersCopy();

        for (SocketWriter sw : socketWritersCopy) {
            sw.sendMessage(messageObj);
        }

        ArrayList<VirtualClient> rmiClientsCopy = getRmiClientsCopy();

        try{
            for (VirtualClient vc : rmiClientsCopy) {
                vc.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastGameMessageToOthers(Object messageObj, SocketWriter swSender, VirtualClient vcSender) {
        ArrayList<SocketWriter> socketWritersCopy = getSocketWritersCopy();

        for (SocketWriter sw : socketWritersCopy) {
            if(!sw.equals(swSender))
                sw.sendMessage(messageObj);
        }

        ArrayList<VirtualClient> rmiClientsCopy = getRmiClientsCopy();

        try{
            for (VirtualClient vc : rmiClientsCopy) {
                if(!vc.equals(vcSender))
                    vc.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTimer() {
        timer.startTimer();
    }
}