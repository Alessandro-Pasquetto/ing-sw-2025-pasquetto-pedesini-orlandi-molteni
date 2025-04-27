package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.*;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BattlezoneTest {

    @Test
    void getCouples() {
        ArrayList<ConditionPenalty> conditionPenalties = new ArrayList<ConditionPenalty>() {{
            add(new ConditionPenalty(ConditionType.CREWREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, 3, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.FIREPOWERREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, 3, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.ENGINEPOWERREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, 3, new ArrayList<Projectile>())));
        }};

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", conditionPenalties);
        assertEquals(conditionPenalties, battlezone.getCouples());
    }

    @Test
    void lessPopulatedSpaceship() {
        Game game = new Game(0, 4, 1);
        Board board = game.getBoard();

        Player p1 = new Player("a", 0, 1);
        Player p2 = new Player("b", 0, 1);
        Player p3 = new Player("c", 0, 1);
        Player p4 = new Player("d", 0, 1);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.addPlayer(p4);

        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);

        board.addTravelersInTrack(1);

        p1.getSpaceship().addCrewCount(7);
        p2.getSpaceship().addCrewCount(5);
        p3.getSpaceship().addCrewCount(3);
        p4.getSpaceship().addCrewCount(3);

        ArrayList<ConditionPenalty> conditionPenalties = new ArrayList<ConditionPenalty>() {{
            add(new ConditionPenalty(ConditionType.CREWREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, -3, null)));
        }};

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", conditionPenalties);

        assertEquals(p3, battlezone.lessPopulatedSpaceship(board.getCopyTravelers()));

        assertEquals(ConditionType.CREWREQUIREMENT, battlezone.getCouples().getFirst().getCondition());
    }

    @Test
    void penaltyDays() {

        Game game = new Game(0, 4, 1);
        Board board = game.getBoard();

        Player p1 = new Player("a", 0, 1);
        Player p2 = new Player("b", 0, 1);
        Player p3 = new Player("c", 0, 1);
        Player p4 = new Player("d", 0, 1);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.addPlayer(p4);

        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);

        board.addTravelersInTrack(1);

        board.movePlayerByDistance(p1, 13);
        board.movePlayerByDistance(p2, 11);
        board.movePlayerByDistance(p3, 10);
        board.movePlayerByDistance(p4, 8);

        ArrayList<ConditionPenalty> conditionPenalties = new ArrayList<ConditionPenalty>() {{
            add(new ConditionPenalty(ConditionType.CREWREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, 3, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.FIREPOWERREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, 3, new ArrayList<Projectile>())));
            add(new ConditionPenalty(ConditionType.ENGINEPOWERREQUIREMENT, new Penalty(PenaltyType.PENALTYDAYS, 3, new ArrayList<Projectile>())));
        }};

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", conditionPenalties);

        battlezone.penaltyDays(board, p1, -4);
        battlezone.penaltyDays(board, p2, -3);
        battlezone.penaltyDays(board, p3, -4);
        battlezone.penaltyDays(board, p4, -2);

//        for (int i = 0; i < board.getTrack().length; i++) {
//            if(board.getTrack()[i] == null)
//                System.out.printf("%-5s", "NULL");
//            else
//                System.out.printf("%-5s", board.getTrack()[i].getName());
//        }
//        System.out.println();
//
//        System.out.println(p1.getPosition() + ", " + p2.getPosition() + ", " + p3.getPosition() + ", " + p4.getPosition());

        assertEquals(board.getTrack()[12], p1);
        assertEquals(board.getTrack()[7], p2);
        assertEquals(board.getTrack()[5], p3);
        assertEquals(board.getTrack()[4], p4);
    }

    @Test
    void chooseDiscardedCrew() {

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", new ArrayList<>());

        Player mario = new Player("mario", 0, 2);

        HousingUnit housingUnit0 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);

        HousingUnit housingUnit1 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnit1.setCrewCount(1);

        HousingUnit housingUnit2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnit2.setCrewCount(2);

        HousingUnit housingUnitAlienOrange = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnitAlienOrange.setAlienOrange(true);

        HousingUnit housingUnitAlienPurple = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnitAlienPurple.setAlienPurple(true);

        assertFalse(battlezone.chooseDiscardedCrew(mario.getSpaceship(), housingUnit0));
        assertTrue(battlezone.chooseDiscardedCrew(mario.getSpaceship(), housingUnit1));
        assertTrue(battlezone.chooseDiscardedCrew(mario.getSpaceship(), housingUnit2));
        assertTrue(battlezone.chooseDiscardedCrew(mario.getSpaceship(), housingUnitAlienOrange));
        assertTrue(battlezone.chooseDiscardedCrew(mario.getSpaceship(), housingUnitAlienPurple));

        assertEquals(0, housingUnit1.getCrewCount());
        assertEquals(1, housingUnit2.getCrewCount());
        assertFalse(housingUnitAlienOrange.getAllowAlienOrange());
        assertFalse(housingUnitAlienOrange.getAllowAlienPurple());
    }

    @Test
    void checkShields() {
        Player player = new Player("a", 0, 1);
        player.getSpaceship().addRightDownShieldCount(2);

        Projectile p1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile p2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile p3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile p4 = new Projectile(ProjectileSize.SMALL, 3);

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", new ArrayList<>());

//        System.out.println(player.getSpaceship().getIdxShieldCount(0) + ", " + player.getSpaceship().getIdxShieldCount(1) + ", " + player.getSpaceship().getIdxShieldCount(2) + ", " + player.getSpaceship().getIdxShieldCount(3));

        assertFalse(battlezone.checkShields(player, p1));
        assertTrue(battlezone.checkShields(player, p2));
        assertTrue(battlezone.checkShields(player, p3));
        assertFalse(battlezone.checkShields(player, p4));
    }


    @Test
    void penaltyShot() {
        Game game1 = new Game(0, 3, 1);

        Player p1 = new Player("a", 0, 1);

        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();
        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(1, 2, 0);

        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(1, 1, 1);

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb1.placeComponent(2, 3, 1);

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb1.placeComponent(3, 2, 1);

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(2, 1, 2);

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        bb1.placeComponent(3, 3, 0);

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2 , "img", new ArrayList<>());

        Projectile s1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile s2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile s3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile s4 = new Projectile(ProjectileSize.SMALL, 3);

        // up
        assertNull(battlezone.penaltyShot(game1, p1, s1, 2));
        assertNull(battlezone.penaltyShot(game1, p1, s1, 4));
        assertNull(battlezone.penaltyShot(game1, p1, s1, 5));
        assertNotNull(battlezone.penaltyShot(game1, p1, s1, 6));
        assertNotNull(battlezone.penaltyShot(game1, p1, s1, 8));
        assertNull(battlezone.penaltyShot(game1, p1, s1, 9));
        assertNull(battlezone.penaltyShot(game1, p1, s1, 10));

        // bottom
        assertNull(battlezone.penaltyShot(game1, p1, s3, 2));
        assertNull(battlezone.penaltyShot(game1, p1, s3, 4));
        assertNull(battlezone.penaltyShot(game1, p1, s3, 5));
        assertNotNull(battlezone.penaltyShot(game1, p1, s3, 6));
        assertNotNull(battlezone.penaltyShot(game1, p1, s3, 8));
        assertNull(battlezone.penaltyShot(game1, p1, s3, 9));
        assertNull(battlezone.penaltyShot(game1, p1, s3, 10));

        // right
        assertNull(battlezone.penaltyShot(game1, p1, s2, 3));
        assertNull(battlezone.penaltyShot(game1, p1, s2, 5));
        assertNotNull(battlezone.penaltyShot(game1, p1, s2, 6));
        assertNotNull(battlezone.penaltyShot(game1, p1, s2, 8));
        assertNull(battlezone.penaltyShot(game1, p1, s2, 9));
        assertNull(battlezone.penaltyShot(game1, p1, s2, 10));

        // left
        assertNull(battlezone.penaltyShot(game1, p1, s4, 3));
        assertNull(battlezone.penaltyShot(game1, p1, s4, 5));
        assertNotNull(battlezone.penaltyShot(game1, p1, s4, 7));
        assertNotNull(battlezone.penaltyShot(game1, p1, s4, 8));
        assertNull(battlezone.penaltyShot(game1, p1, s4, 9));
        assertNull(battlezone.penaltyShot(game1, p1, s4, 10));


        Game game2 = new Game(1, 3, 2);

        Player p2 = new Player("a", 1, 2);

        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        bb2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb2.placeComponent(3, 1, 0);

        bb2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb2.placeComponent(2, 1, 1);

        bb2.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb2.placeComponent(3, 3, 1);

        bb2.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb2.placeComponent(2, 3, 1);

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb2.placeComponent(4, 2, 1);

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb2.placeComponent(4, 3, 0);

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb2.placeComponent(1, 3, 2);

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb2.placeComponent(5, 3, 0);

        Battlezone battlezone2 = new Battlezone(CardType.BATTLEZONE, 1, "img", new ArrayList<>());

        Projectile ss1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile ss2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile ss3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile ss4 = new Projectile(ProjectileSize.SMALL, 3);

        // up
        assertNull(battlezone2.penaltyShot(game2, p2, ss1, 2));
        assertNull(battlezone2.penaltyShot(game2, p2, ss1, 4));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss1, 5));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss1, 6));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss1, 8));
        assertNull(battlezone2.penaltyShot(game2, p2, ss1, 10));
        assertNull(battlezone2.penaltyShot(game2, p2, ss1, 12));

        // bottom
        assertNull(battlezone2.penaltyShot(game2, p2, ss3, 2));
        assertNull(battlezone2.penaltyShot(game2, p2, ss3, 4));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss3, 5));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss3, 6));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss3, 9));
        assertNull(battlezone2.penaltyShot(game2, p2, ss3, 10));

        // right
        assertNull(battlezone2.penaltyShot(game2, p2, ss2, 3));
        assertNull(battlezone2.penaltyShot(game2, p2, ss2, 5));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss2, 6));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss2, 8));
        assertNull(battlezone2.penaltyShot(game2, p2, ss2, 9));
        assertNull(battlezone2.penaltyShot(game2, p2, ss2, 10));

        // left
        assertNull(battlezone2.penaltyShot(game2, p2, ss4, 3));
        assertNull(battlezone2.penaltyShot(game2, p2, ss4, 5));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss4, 7));
        assertNotNull(battlezone2.penaltyShot(game2, p2, ss4, 8));
        assertNull(battlezone2.penaltyShot(game2, p2, ss4, 9));
        assertNull(battlezone2.penaltyShot(game2, p2, ss4, 10));
    }

    @Test
    void chooseDiscardedBox() {
        Spaceship s = new Spaceship(1, 0);
        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 1, "img", new ArrayList<>());

        BoxStorage bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "", 3);
        bs1.addBox(s, Box.YELLOW, 2);
        bs1.addBox(s, Box.GREEN, 1);

//        for (int i = 0; i < bs1.getCapacity(); i++) {
//            if(bs1.getBoxStorage()[i] == null)
//                System.out.print("NULL, ");
//            else
//                System.out.print(bs1.getBoxStorage()[i].getValue() + ", ");
//        }
//        System.out.println();

        assertFalse(battlezone.chooseDiscardedBox(s, bs1, 0));
        assertFalse(battlezone.chooseDiscardedBox(s, bs1, 1));
        assertNotEquals(null, (bs1.getBoxStorage()[1]));
        assertTrue(battlezone.chooseDiscardedBox(s, bs1, 2));
        assertTrue(battlezone.chooseDiscardedBox(s, bs1, 1));
        assertNull(bs1.getBoxStorage()[2]);

//        for (int i = 0; i < bs1.getCapacity(); i++) {
//            if(bs1.getBoxStorage()[i] == null)
//                System.out.print("NULL, ");
//            else
//                System.out.print(bs1.getBoxStorage()[i].getValue() + ", ");
//        }
//        System.out.println();

        BoxStorage bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "", 3);

        bs2.addBox(s, Box.GREEN, 0);
        bs2.addBox(s, Box.RED, 2);

//        for (int i = 0; i < bs2.getCapacity(); i++) {
//            if(bs2.getBoxStorage()[i] == null)
//                System.out.print("NULL, ");
//            else
//                System.out.print(bs2.getBoxStorage()[i].getValue() + ", ");
//        }
//        System.out.println();

        assertFalse(battlezone.chooseDiscardedBox(s, bs2, 0));
        assertFalse(battlezone.chooseDiscardedBox(s, bs2, 1));
        assertTrue(battlezone.chooseDiscardedBox(s, bs2, 2));
        assertFalse(battlezone.chooseDiscardedBox(s, bs2, 3));

        BoxStorage bs3 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "", 3);

        bs3.addBox(s, Box.GREEN, 0);
        bs3.addBox(s, Box.BLUE, 1);

        assertTrue(battlezone.chooseDiscardedBox(s, bs2, 0));
        assertFalse(battlezone.chooseDiscardedBox(s, bs3, 1));
        assertTrue(battlezone.chooseDiscardedBox(s, bs3, 0));
        assertFalse(battlezone.chooseDiscardedBox(s, bs1, 2));
        assertTrue(battlezone.chooseDiscardedBox(s, bs3, 1));
        assertFalse(battlezone.chooseDiscardedBox(s, bs3, 2));
    }

    @Test
    void chooseDiscardedBattery() {

        Player mario = new Player("mario", 0, 2);

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", new ArrayList<>());
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(mario.getSpaceship(), 2);

        // Returns false if component is not a Housing Unit
        assertFalse(battlezone.chooseDiscardedBattery(mario.getSpaceship(), (BatteryStorage) notBattery));

        // Removes one battery member from the Housing Unit
        assertTrue(battlezone.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(battlezone.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(battlezone.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(0, battery.getItemsCount());
    }
}