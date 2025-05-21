package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Test
    void ready() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 2, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);

        // Verify that the player is not ready initially
        assertFalse(player.getIsReady());

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {
            }
        };

        assertEquals(GamePhase.WAITING, gameManager.getGame().getPhase());
        // Call the ready method and ensure no exceptions are thrown
        GameController.ready(gameManager, player, sender);

        //todo
    }
}