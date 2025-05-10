package org.progetto.server.controller.events;
import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.EventCommon.DiceResultMessage;
import org.progetto.server.connection.Sender;
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
        GameManager gameManager = new GameManager(0, 2, 1);

        ArrayList<Projectile> meteors = new ArrayList<Projectile>();

        meteors.add(new Projectile(ProjectileSize.SMALL, 0));
        meteors.add(new Projectile(ProjectileSize.SMALL, 1));
        meteors.add(new Projectile(ProjectileSize.BIG, 1));
        meteors.add(new Projectile(ProjectileSize.BIG, 0));

        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN, 2, "imgSrc", meteors);
        gameManager.getGame().setActiveEventCard(meteorsRain);

        Player p1 = new Player("mario", 0, 1) {
            int count = 0;

            @Override
            public int rollDice() {
                int result = switch (count) {
                    case 0 -> 7;
                    case 1 -> 8;
                    case 2 -> 7;
                    case 3 -> 6;
                    default -> 0;
                };

                count++;

                return result;
            }
        };
        Player p2 = new Player("alice", 1, 1);

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {

            }
        };

        gameManager.addSender(p1, sender);
        gameManager.addSender(p2, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);

        //player 1 spaceship setup
        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(1, 2, 0);

        bb1.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(3, 2, 1);

        BatteryStorage storage = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 0, 3, 3}, "imgPath", 3);
        storage.incrementItemsCount(p1.getSpaceship(), 3);
        bb1.setHandComponent(storage);
        bb1.placeComponent(2, 3, 0);

        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(2, 1, 0);

        bb1.initSpaceshipParams();

        //player 2 spaceship setup
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb2.placeComponent(1, 2, 0);

        bb2.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb2.placeComponent(3, 2, 1);

        bb2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        bb2.placeComponent(2, 1, 0);

        bb2.initSpaceshipParams();

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

        // First meteor
        Thread.sleep(200);
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p1, sender);

        Thread.sleep(200);
        assertNotNull(p1.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[1][2]);
        assertNull(p2.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[1][2]);
        assertEquals(EventPhase.PROTECTION_DECISION, controller.getPhase());
        controller.receiveProtectionDecision(p1, "YES", sender);

        Thread.sleep(200);
        controller.receiveDiscardedBatteries(p1, 2, 3, sender);

        Thread.sleep(200);
        assertNotNull(p1.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[1][2]);

        // Second meteor
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p1, sender);

        Thread.sleep(200);
        assertNotNull(p1.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[3][2]);

        // Third meteor
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p1, sender);

        Thread.sleep(200);
        assertNotNull(p1.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[2][3]);
        assertNull(p2.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[2][3]);
        assertEquals(EventPhase.PROTECTION_DECISION, controller.getPhase());
        controller.receiveProtectionDecision(p1, "NO", sender);

        Thread.sleep(200);
        assertNull(p1.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[2][3]);

        // Fourth meteor
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p1, sender);

        Thread.sleep(200);
        assertNotNull(p1.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[2][1]);
        assertNotNull(p2.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[2][1]);
    }
}