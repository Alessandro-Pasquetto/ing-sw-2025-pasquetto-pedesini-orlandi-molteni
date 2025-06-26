package org.progetto.server.connection;

import java.rmi.RemoteException;
import java.util.HashMap;

public class ServerDisconnectionDetection {

    private static final int MAX_ALLOWED_MISSED_PINGS = 1;

    public record IsWaitingPong(int missedPongCountLobby, int missedPongCountGame) {}

    private static final HashMap<Sender, IsWaitingPong> senderPongMap = new HashMap<>();

    private static void setIsWaitingPongLobby(Sender sender, boolean isWaitingPongLobby) {
        int missedPongCountLobby = senderPongMap.get(sender).missedPongCountLobby();
        int missedPongCountGame = senderPongMap.get(sender).missedPongCountGame();

        if(isWaitingPongLobby)
            missedPongCountLobby++;
        else
            missedPongCountLobby = 0;

        IsWaitingPong updated = new IsWaitingPong(missedPongCountLobby, missedPongCountGame);
        senderPongMap.put(sender, updated);
    }

    private static void setIsWaitingPongGame(Sender sender, boolean isWaitingGame) {
        int missedPongCountLobby = senderPongMap.get(sender).missedPongCountLobby();
        int missedPongCountGame = senderPongMap.get(sender).missedPongCountGame();

        if(isWaitingGame)
            missedPongCountGame++;
        else
            missedPongCountGame = 0;

        IsWaitingPong updated = new IsWaitingPong(missedPongCountLobby, missedPongCountGame);
        senderPongMap.put(sender, updated);
    }

    public static void setPongIsArrived(Sender sender){
        senderPongMap.put(sender, new IsWaitingPong(0, 0));
    }

    public static void ping(Runnable action, String where, Sender sender) {
        senderPongMap.computeIfAbsent(sender, _ -> new IsWaitingPong(0, 0));

        if(where.equals("Game"))
            setIsWaitingPongLobby(sender, false);

        int missedPongCountLobby = senderPongMap.get(sender).missedPongCountLobby();
        int missedPongCountGame = senderPongMap.get(sender).missedPongCountGame();

        if((where.equals("Lobby") && missedPongCountLobby > MAX_ALLOWED_MISSED_PINGS) || (where.equals("Game") && missedPongCountGame > MAX_ALLOWED_MISSED_PINGS)){
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
            } catch (RemoteException _) {
            }
        }).start();
    }
}