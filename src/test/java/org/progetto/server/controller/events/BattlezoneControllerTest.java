package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
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

class BattlezoneControllerTest {

    @Test
    void battlezoneControllerTest() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);

        ArrayList<Projectile> projectiles = new ArrayList<>();
        projectiles.add(new Projectile(ProjectileSize.SMALL, 0));
        projectiles.add(new Projectile(ProjectileSize.BIG, 3));

        ArrayList<ConditionPenalty> conditionPenalties = new ArrayList<ConditionPenalty>() {{
            add(new ConditionPenalty(ConditionType.CREWREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, -3, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.FIREPOWERREQUIREMENT, new Penalty(PenaltyType.PENALTYCREW, 2, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.ENGINEPOWERREQUIREMENT, new Penalty(PenaltyType.PENALTYBOXES, 3, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.CREWREQUIREMENT, new Penalty(PenaltyType.PENALTYSHOTS, 3, projectiles)));

        }};
        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", conditionPenalties);

        gameManager.getGame().setActiveEventCard(battlezone);

        Player p1 = new Player("mario", 0, 1);
        Player p2 = new Player("alice", 1, 1);
        Player p3 = new Player("alessio", 2, 1) {
            int count = 0;

            @Override
            public int rollDice(){
                int result = switch(count){
                    case 0 -> 3;
                    case 1 -> 1;
                    default -> 2;
                };

                count++;
                return result;
            }
        };

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

        // p1 will be effect by penalty crew
        HousingUnit hu1 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "img", 2);
        HousingUnit hu2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "img", 2);
        HousingUnit hu3 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "img", 2);

        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();
        bb1.setHandComponent(hu1);
        bb1.placeComponent(2, 1, 0);
        bb1.setHandComponent(hu2);
        bb1.placeComponent(1, 1, 0);
        bb1.setHandComponent(hu3);
        bb1.placeComponent(3, 1, 0);

        hu1.setCrewCount(2);
        hu2.setCrewCount(2);
        hu3.setCrewCount(2);

        p1.getSpaceship().addCrewCount(6);

        // p2 spaceship
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        BatteryStorage batteryStorage = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1,1,1,1}, "img", 3);
        bb2.setHandComponent(batteryStorage);
        bb2.placeComponent(2, 1, 0);

        batteryStorage.incrementItemsCount(p3.getSpaceship(), 3);

        // p3 will be effect by penalty boxes
        BoxStorage bx1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BoxStorage bx2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);

        BuildingBoard bb3 = p3.getSpaceship().getBuildingBoard();
        bb3.setHandComponent(bx1);
        bb3.placeComponent(2, 1, 0);
        bb3.setHandComponent(bx2);
        bb3.placeComponent(1, 1, 0);
        bb3.setHandComponent(batteryStorage);
        bb3.placeComponent(1, 2, 0);

        batteryStorage.incrementItemsCount(p3.getSpaceship(), 3);
        p3.getSpaceship().addBatteriesCount(3);

        bx1.addBox(p3.getSpaceship(), Box.BLUE, 1);
        bx2.addBox(p3.getSpaceship(), Box.RED, 0);

        // crew count
        p2.getSpaceship().addCrewCount(7);
        p3.getSpaceship().addCrewCount(2);

        // fire/shooting power
        p1.getSpaceship().addNormalShootingPower(3);
        p2.getSpaceship().addNormalShootingPower(5);
        p2.getSpaceship().addFullDoubleCannonCount(1);
        p3.getSpaceship().addNormalShootingPower(5);
        p3.getSpaceship().addFullDoubleCannonCount(2);

        // engine power
        p1.getSpaceship().addNormalEnginePower(5);
        p2.getSpaceship().addNormalEnginePower(5);
        p2.getSpaceship().addDoubleEngineCount(1);
        p3.getSpaceship().addNormalEnginePower(1);
        p3.getSpaceship().addDoubleEngineCount(2);

        // Controller
        BattlezoneController controller = new BattlezoneController(gameManager);

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
        assertEquals(-3, p3.getPosition());
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        controller.receiveHowManyCannonsToUse(p2, 0, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());
        controller.receiveHowManyCannonsToUse(p3, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());
        controller.receiveDiscardedBatteries(p3, 1, 2, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());
        controller.receiveDiscardedCrew(p1, 2, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());
        controller.receiveDiscardedCrew(p1, 3, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.ENGINE_NUMBER, controller.getPhase());
        controller.receiveHowManyEnginesToUse(p2, 0, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.ENGINE_NUMBER, controller.getPhase());
        controller.receiveHowManyEnginesToUse(p3, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());
        controller.receiveDiscardedBatteries(p3, 1, 2, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());
        controller.receiveDiscardedBox(p3, 2, 1, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());
        controller.receiveDiscardedBox(p3, 1, 1, 0, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());
        controller.receiveDiscardedBox(p3, 2, 1, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BATTERIES_FOR_BOXES, controller.getPhase());
        controller.receiveDiscardedBatteries(p3, 1, 2, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p3, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.ROLL_DICE, controller.getPhase());
        controller.rollDice(p3, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.CONDITION, controller.getPhase());
    }
}