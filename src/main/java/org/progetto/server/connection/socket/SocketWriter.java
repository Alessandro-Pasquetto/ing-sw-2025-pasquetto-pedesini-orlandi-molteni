package org.progetto.server.connection.socket;

import org.progetto.server.connection.Sender;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SocketWriter implements Sender {

    private final ObjectOutputStream out;
    private boolean isWaitingPongLobby;
    private boolean isWaitingPongGame;

    public SocketWriter(ObjectOutputStream out) {
        this.out = out;
        this.isWaitingPongLobby = false;
        this.isWaitingPongGame = false;
    }

    public synchronized void setPongReceived() {
        this.isWaitingPongLobby = false;
        this.isWaitingPongGame = false;
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public synchronized void sendMessage(Object messageObj) {
        try {
            out.reset();
            out.writeObject(messageObj);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void ping(Runnable action, String where) {
        if(where.equals("Game"))
            isWaitingPongLobby = false;

        if(where.equals("Lobby") && isWaitingPongLobby || where.equals("Game") && isWaitingPongGame){
            System.err.println("Player disconnected: pong timeout");
            action.run();
            return;
        }

        try {
            out.reset();
            out.writeObject("Ping");
            out.flush();

            if(where.equals("Lobby")) {
                isWaitingPongLobby = true;
            }
            else if (where.equals("Game")) {
                isWaitingPongGame= true;
            }


        } catch (IOException e) {
            e.printStackTrace();
            action.run();
        }
    }
}