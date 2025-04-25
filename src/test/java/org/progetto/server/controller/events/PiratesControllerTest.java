package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.*;

import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PiratesControllerTest {

    @Test
    void piratesControllerTest() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);
        ArrayList<Projectile> projectiles = new ArrayList<>();
        projectiles.add(new Projectile(ProjectileSize.SMALL, 0));
        projectiles.add(new Projectile(ProjectileSize.BIG, 3));
        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, projectiles);
        gameManager.getGame().setActiveEventCard(pirates);

        Player p1 = new Player("mario", 0, 1) {
            int count = 0;

            @Override
            public int rollDice(){
                int result = switch(count){
                    case 0 -> 2;
                    case 1 -> 1;
                    default -> 2;
                };

                count++;
                return result;
            }
        };
        Player p2 = new Player("alice", 0, 1);
        Player p3 = new Player("alessio", 0, 1);

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        VirtualClient sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        gameManager.addRmiClient(p1, sender);
        gameManager.addRmiClient(p2, sender);
        gameManager.addRmiClient(p3, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();
        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(2, 1, 0);

        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(1, 1, 1);

        BatteryStorage batteryStorage1 = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1,1,1,1}, "img", 2);
        bb1.setHandComponent(batteryStorage1);
        bb1.placeComponent(3, 2, 1);

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb1.placeComponent(2, 3, 1);

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(1, 2, 2);

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        bb1.placeComponent(3, 3, 0);

        bb1.initSpaceshipParams();

        BatteryStorage batteryStorage2 = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        bb2.setHandComponent(batteryStorage2);
        bb2.placeComponent(2, 1, 0);

        p2.getSpaceship().addBatteriesCount(2);

        BatteryStorage batteryStorage = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BuildingBoard bb3 = p3.getSpaceship().getBuildingBoard();
        bb3.setHandComponent(batteryStorage);
        bb3.placeComponent(2, 1, 0);

        batteryStorage.incrementItemsCount(p3.getSpaceship(), 2);
        p3.getSpaceship().addBatteriesCount(2);

        p1.getSpaceship().addNormalShootingPower(3);
        p2.getSpaceship().addNormalShootingPower(5);
        p2.getSpaceship().addFullDoubleCannonCount(1);
        p3.getSpaceship().addNormalShootingPower(5);
        p3.getSpaceship().addFullDoubleCannonCount(2);

        // Controller
        PiratesController controller = new PiratesController(gameManager);

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
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        Thread.sleep(200);
        controller.receiveHowManyCannonsToUse(p2, 0, sender);

        Thread.sleep(500);
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        Thread.sleep(200);
        controller.receiveHowManyCannonsToUse(p3, 1, sender);
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());

        Thread.sleep(200);
        controller.receiveDiscardedBatteries(p3, 2, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.REWARD_DECISION, controller.getPhase());

        Thread.sleep(200);
        controller.receiveRewardDecision(p3, "YES", sender);
        assertEquals(3, p3.getCredits());
        assertEquals(-3, p3.getPosition());

        Thread.sleep(200);
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.SHIELD_DECISION, controller.getPhase());
        controller.receiveProtectionDecision(p1, "YES", sender);

        Thread.sleep(200);
        assertEquals(EventPhase.SHIELD_BATTERY, controller.getPhase());
        controller.receiveDiscardedBatteries(p1, 3, 2, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.HANDLE_SHOT, controller.getPhase());
    }
}