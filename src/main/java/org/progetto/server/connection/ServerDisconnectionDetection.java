package org.progetto.server.connection;

import java.util.HashMap;

public class ServerDisconnectionDetection {

    public record IsWaitingPong(boolean isWaitingPongLobby, boolean isWaitingPongGame) {}

    private static final HashMap<Sender, IsWaitingPong> senderPongMap = new HashMap<>();

    private static void setIsWaitingPongLobby(Sender sender, boolean isWaitingPongLobby) {
        boolean isWaitingPongGame = senderPongMap.get(sender).isWaitingPongGame();

        IsWaitingPong updated = new IsWaitingPong(isWaitingPongLobby, isWaitingPongGame);
        senderPongMap.put(sender, updated);
    }

    private static void setIsWaitingPongGame(Sender sender, boolean isWaitingGame) {
        boolean isWaitingPongLobby = senderPongMap.get(sender).isWaitingPongLobby();

        IsWaitingPong updated = new IsWaitingPong(isWaitingPongLobby, isWaitingGame);
        senderPongMap.put(sender, updated);
    }

    public static void setPongIsArrived(Sender sender){
        senderPongMap.put(sender, new IsWaitingPong(false, false));
    }

    public static void ping(Runnable action, String where, Sender sender) {

        senderPongMap.computeIfAbsent(sender, s -> new IsWaitingPong(false, false));

        if(where.equals("Game"))
            setIsWaitingPongLobby(sender, false);

        boolean isWaitingPongLobby = senderPongMap.get(sender).isWaitingPongLobby();
        boolean isWaitingPongGame = senderPongMap.get(sender).isWaitingPongGame();

        if(where.equals("Lobby") && isWaitingPongLobby || where.equals("Game") && isWaitingPongGame){
            System.err.println("Player disconnected: pong timeout");
            action.run();
            senderPongMap.remove(sender);
            return;
        }

        if(where.equals("Lobby")) {
            setIsWaitingPongLobby(sender, true);
        }
        else if (where.equals("Game")) {
            setIsWaitingPongGame(sender, true);
        }

        new Thread(() -> {
            try {
                sender.sendPing();
            } catch (Exception e) {
                action.run();
                senderPongMap.remove(sender);
                e.printStackTrace();
            }
        }).start();
    }
}