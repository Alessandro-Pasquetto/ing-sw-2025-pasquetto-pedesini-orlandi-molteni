package org.progetto.server.controller;
import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventController;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventControllerTest {

    @Test
    void testEventControllerGeneralFlow() {
        GameManager gm = new GameManager(0, 3, 1);

        Player p1 = new Player("alice");
        Player p2 = new Player("bob");
        gm.getGame().addPlayer(p1);
        gm.getGame().addPlayer(p2);
        gm.getGame().initPlayersSpaceship();

        List<Object> messages = new ArrayList<>();

        Sender sender1 = new Sender() {
            @Override
            public void sendMessage(Object msg) {
                messages.add(msg);
            }

            @Override
            public void sendPing() {

            }
        };

        Sender sender2 = new Sender() {
            @Override
            public void sendMessage(Object msg) {
                messages.add(msg);
            }

            @Override
            public void sendPing() {

            }
        };

        gm.addSender(p1, sender1);
        gm.addSender(p2, sender2);

        EventController.pickEventCard(gm);

        // handleDefeatedPlayers
        gm.getGame().setPhase(GamePhase.TRAVEL);
        gm.getGame().getBoard().addTraveler(p1);
        gm.getGame().getBoard().addTraveler(p2);
        p1.getSpaceship().addCrewCount(-10); // forza "no crew"
        gm.getGame().getBoard().movePlayerByDistance(p2, -30); // forza "lapped"
        EventController.handleDefeatedPlayers(gm);

        // chooseToContinueTravel
        p1.setIsReady(false);
        p2.setIsReady(false);

        EventController.chooseToContinueTravel(gm, "YES", p1, sender1);
        EventController.chooseToContinueTravel(gm, "NO", p2, sender2);
        EventController.chooseToContinueTravel(gm, "INVALID", p1, sender1);

        // no more choises
        gm.getGame().setPhase(GamePhase.BUILDING);
        EventController.chooseToContinueTravel(gm, "YES", p1, sender1);

        // at least one message sended
        assertFalse(messages.isEmpty());
    }
}