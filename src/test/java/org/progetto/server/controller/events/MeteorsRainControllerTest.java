package org.progetto.server.controller.events;
import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.EventCommon.DiceResultMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MeteorsRainControllerTest {

    @Test
    void MeteorsRainControllerTest() throws InterruptedException, RemoteException {

        //board setup
        GameManager gameManager = new GameManager(0,2,1);

        ArrayList<Projectile> meteors = new ArrayList<Projectile>();
        //small meteor from above
        meteors.add(new Projectile(ProjectileSize.SMALL,0));
        //small meteor from the right
        meteors.add(new Projectile(ProjectileSize.SMALL,1));
        //Big meteor from under
        meteors.add(new Projectile(ProjectileSize.BIG,2));
        //Big meteor from the left
        meteors.add(new Projectile(ProjectileSize.BIG,3));

        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN,2,"imgSrc",meteors);
        gameManager.getGame().setActiveEventCard(meteorsRain);


        Player p1 = new Player("mario", 0, 1);
        Player p2 = new Player("alice", 1, 1);


        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);

        VirtualClient sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        gameManager.addRmiClient(p1, sender);
        gameManager.addRmiClient(p2, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);

        //player_1 spaceship setup
        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(1, 2, 0);

        bb1.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(3, 2, 1);

        BatteryStorage storage = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath",3);
        storage.incrementItemsCount(p1.getSpaceship(),3);
        bb1.setHandComponent(storage);
        bb1.placeComponent(2, 3, 0);

        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(2, 1, 0);


        //player_2 spaceship setup
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb2.placeComponent(1, 2, 0);

        bb2.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb2.placeComponent(3, 2, 1);

        bb2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        bb2.placeComponent(2, 1, 0);


        //controller
        MeteorsRainController controller = new MeteorsRainController(gameManager);

        GameThread gameThread = new GameThread(gameManager) {

            @Override
            public void run(){
                try {
                    controller.start();
                } catch (RemoteException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200);
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        Thread.sleep(200);

        //Test not your turn
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotYourTurn", message);

            }
        };
        controller.rollDice(p2,sender);

        //Test correct dice trow
        final DiceResultMessage[] result = {null};
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                result[0] = (DiceResultMessage) message;

            }
        };
        controller.rollDice(p1,sender);
        assertNotNull(result[0]);
        System.out.println(result[0].getDiceResult());

        Thread.sleep(200);

        //Testing first meteor
        //assertEquals(EventPhase.HANDLE_SMALL_METEOR, controller.getPhase());
    }
}