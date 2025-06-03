package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.LostShip;
import org.progetto.server.model.events.Stardust;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class StardustControllerTest {

    @Test
    void stardustControllerTest() throws InterruptedException, RemoteException {
        GameManager gameManager = new GameManager(0, 3, 1);
        Stardust stardust = new Stardust(CardType.STARDUST, 1, "imgSrc");
        gameManager.getGame().setActiveEventCard(stardust);

        Player p1 = new Player("mario");
        Player p2 = new Player("alice");
        Player p3 = new Player("alessio");

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        gameManager.getGame().initPlayersSpaceship();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }
        };

        gameManager.addSender(p1, sender);
        gameManager.addSender(p2, sender);
        gameManager.addSender(p3, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        p1.getSpaceship().setExposedConnectorsCount(2);
        p2.getSpaceship().setExposedConnectorsCount(3);
        p3.getSpaceship().setExposedConnectorsCount(1);

        StardustController controller = new StardustController(gameManager);

        GameThread gameThread = new GameThread(gameManager) {

            @Override
            public void run(){
                controller.start();
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200);
        assertEquals(EventPhase.EFFECT, controller.getPhase());

        assertEquals(-3, p1.getPosition());
        assertEquals(-4, p2.getPosition());
        assertEquals(-1, p3.getPosition());
    }
}