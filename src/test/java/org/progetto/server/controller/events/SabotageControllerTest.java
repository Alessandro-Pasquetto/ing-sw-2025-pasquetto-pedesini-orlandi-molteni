package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.Sabotage;

public class SabotageControllerTest {

    @Test
    void testSabotageControllerFlow() {
        GameManager gm = new GameManager(0, 3, 2);

        Player p1 = new Player("alice");
        Player p2 = new Player("bob");

        gm.getGame().addPlayer(p1);
        gm.getGame().addPlayer(p2);
        gm.getGame().initPlayersSpaceship();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }
        };

        gm.addSender(p1, sender);
        gm.addSender(p2, sender);

        gm.getGame().getBoard().addTraveler(p1);
        gm.getGame().getBoard().addTraveler(p2);

        Sabotage sabotageCard = new Sabotage(CardType.SABOTAGE, 2, "imgSrc"); // usa implementazione reale
        gm.getGame().setActiveEventCard(sabotageCard);

        SabotageController controller = new SabotageController(gm);

        GameThread gameThread = new GameThread(gm) {

            @Override
            public void run(){
                controller.start();
            }
        };

        gm.setGameThread(gameThread);
        gameThread.start();

        controller.rollDice(p1, sender);
        controller.rollDice(p1, sender);

        controller.rollDice(p1, sender);
        controller.rollDice(p1, sender);

        controller.rollDice(p1, sender);
        controller.rollDice(p1, sender);
    }
}