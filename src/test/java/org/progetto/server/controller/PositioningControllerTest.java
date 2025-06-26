package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PositioningControllerTest {

    @Test
    void positioningControllerTest() throws InterruptedException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player p1 = new Player("mario");
        Player p2 = new Player("alice");
        Player p3 = new Player("luca");

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        gameManager.getGame().setPhase(GamePhase.POSITIONING);

        List<Object> sentMessagesP1 = new ArrayList<>();
        List<Object> sentMessagesP2 = new ArrayList<>();
        List<Object> sentMessagesP3 = new ArrayList<>();

        Sender sender1 = new Sender() {
            @Override
            public void sendMessage(Object msg) {
                sentMessagesP1.add(msg);
            }

            @Override
            public void sendPing() {

            }
        };

        Sender sender2 = new Sender() {
            @Override
            public void sendMessage(Object msg) {
                sentMessagesP2.add(msg);
            }

            @Override
            public void sendPing() {

            }
        };

        Sender sender3 = new Sender() {
            @Override
            public void sendMessage(Object msg) {
                sentMessagesP3.add(msg);
            }

            @Override
            public void sendPing() {

            }
        };

        gameManager.addSender(p1, sender1);
        gameManager.addSender(p2, sender2);
        gameManager.addSender(p3, sender3);

        GameThread testThread = new GameThread(gameManager) {
            int callCount = 0;

            @Override
            public void resetAndWaitTravelerReady(Player player) {
                callCount++;
                if (player.equals(p1)) {
                    p1.setIsReady(true); // p1 ready
                } else if (player.equals(p2)) {
                    p2.setIsReady(false); // p2 not ready
                } else if (player.equals(p3)) {
                    p3.setIsReady(true); // p3 ready
                }
            }

            @Override
            public void notifyThread() {
            }
        };
        gameManager.setGameThread(testThread);

        PositioningController.askForStartingPosition(gameManager);

        // AskStartingPositionMessage for each player
        assertTrue(sentMessagesP1.stream().anyMatch(m -> m instanceof AskStartingPositionMessage));
        assertTrue(sentMessagesP2.stream().anyMatch(m -> m instanceof AskStartingPositionMessage));
        assertTrue(sentMessagesP3.stream().anyMatch(m -> m instanceof AskStartingPositionMessage));

        // ActivePlayerMessage for each player
        assertTrue(sentMessagesP1.stream().anyMatch(m -> m instanceof ActivePlayerMessage));
        assertTrue(sentMessagesP2.stream().anyMatch(m -> m instanceof ActivePlayerMessage));
        assertTrue(sentMessagesP3.stream().anyMatch(m -> m instanceof ActivePlayerMessage));

        // insertAtFurthestStartPosition (p2 not ready)
        Player[] startingPositions = gameManager.getGame().getBoard().getStartingPositionsCopy();
        boolean p2Inserted = false;
        for (Player posPlayer : startingPositions) {
            if (posPlayer != null && posPlayer.equals(p2)) {
                p2Inserted = true;
                break;
            }
        }
        assertTrue(p2Inserted, "p2 deve essere inserito automaticamente in posizione disponibile");

        sentMessagesP1.clear();

        gameManager.getGame().setActivePlayer(p1);
        PositioningController.receiveStartingPosition(gameManager, p1, 1, sender1);
        assertTrue(sentMessagesP1.stream().anyMatch(m -> m.equals("ValidStartingPosition")));
        assertTrue(sentMessagesP1.stream().anyMatch(m -> m instanceof StartingPositionsMessage));
        assertTrue(p1.getIsReady());

        // wrong turn
        sentMessagesP2.clear();
        gameManager.getGame().setActivePlayer(p1);
        PositioningController.receiveStartingPosition(gameManager, p2, 1, sender2);
        assertTrue(sentMessagesP2.contains("NotYourTurn"));

        // wrong phase
        sentMessagesP3.clear();
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        PositioningController.receiveStartingPosition(gameManager, p3, 2, sender3);
        assertTrue(sentMessagesP3.contains("IncorrectPhase"));

        gameManager.getGame().setPhase(GamePhase.POSITIONING);

        // already taken position
        sentMessagesP2.clear();
        gameManager.getGame().setActivePlayer(p2);
        PositioningController.receiveStartingPosition(gameManager, p2, 0, sender2);
        assertTrue(sentMessagesP2.contains("StartingPositionAlreadyTaken"));

        // not valid position (-1)
        sentMessagesP2.clear();
        PositioningController.receiveStartingPosition(gameManager, p2, -1, sender2);
        assertTrue(sentMessagesP2.contains("InvalidStartingPosition"));

        // showStartingPositions
        sentMessagesP1.clear();
        PositioningController.showStartingPositions(gameManager, sender1);
        assertTrue(sentMessagesP1.stream().anyMatch(m -> m instanceof StartingPositionsMessage));

        // test with wrong phase
        sentMessagesP1.clear();
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        PositioningController.showStartingPositions(gameManager, sender1);
        assertTrue(sentMessagesP1.contains("IncorrectPhase"));
        gameManager.getGame().setPhase(GamePhase.POSITIONING);

        // showPlayersInPositioningDecisionOrder
        sentMessagesP2.clear();
        PositioningController.showPlayersInPositioningDecisionOrder(gameManager, sender2);
        assertTrue(sentMessagesP2.stream().anyMatch(m -> m instanceof PlayersInPositioningDecisionOrderMessage));

        // test reconnection player
        sentMessagesP3.clear();
        gameManager.getGame().setActivePlayer(p3);
        PositioningController.reconnectPlayer(gameManager, p3, sender3);
        assertTrue(sentMessagesP3.stream().anyMatch(m -> m instanceof AskStartingPositionMessage));

        // not active player: no message
        sentMessagesP3.clear();
        gameManager.getGame().setActivePlayer(p1);
        PositioningController.reconnectPlayer(gameManager, p3, sender3);
        assertTrue(sentMessagesP3.isEmpty());
    }
}