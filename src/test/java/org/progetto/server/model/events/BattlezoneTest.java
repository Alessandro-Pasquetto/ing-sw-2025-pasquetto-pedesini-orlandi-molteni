package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BattlezoneTest {

    @Test
    void getCouples() {
    }

    @Test
    void penaltyDays() {

        Game game = new Game(0, 3, 1);
        Board board = game.getBoard();

        Player p1 = new Player("a", 0, 1);
        Player p2 = new Player("b", 0, 1);
        Player p3 = new Player("c", 0, 1);
        Player p4 = new Player("d", 0, 1);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.addPlayer(p4);

        board.addTraveler(p1, 1);
        board.addTraveler(p2, 1);
        board.addTraveler(p3, 1);
        board.addTraveler(p4, 1);

        board.movePlayerByDistance(p1, 13);
        board.movePlayerByDistance(p2, 11);
        board.movePlayerByDistance(p3, 10);
        board.movePlayerByDistance(p4, 8);

        for (int i = 0; i < board.getTrack().length; i++) {
            if(board.getTrack()[i] == null)
                System.out.printf("%-5s", "NULL");
            else
                System.out.printf("%-5s", board.getTrack()[i].getName());
        }
        System.out.println();



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

        for (int i = 0; i < board.getTrack().length; i++) {
            if(board.getTrack()[i] == null)
                System.out.printf("%-5s", "NULL");
            else
                System.out.printf("%-5s", board.getTrack()[i].getName());
        }
        System.out.println();

        System.out.println(p1.getPosition() + ", " + p2.getPosition() + ", " + p3.getPosition() + ", " + p4.getPosition());

        assertEquals(board.getTrack()[12], p1);
        assertEquals(board.getTrack()[7], p2);
        assertEquals(board.getTrack()[5], p3);
        assertEquals(board.getTrack()[4], p4);
    }

    @Test
    void chooseDiscardedCrew() {

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", new ArrayList<>());

        Player mario = new Player("mario",0,2);

        HousingUnit housingUnit0 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);

        HousingUnit housingUnit1 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnit1.setCrewCount(1);

        HousingUnit housingUnit2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnit2.setCrewCount(2);

        HousingUnit housingUnitAlienOrange = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnitAlienOrange.setAlienOrange(true);

        HousingUnit housingUnitAlienPurple = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        housingUnitAlienPurple.setAlienPurple(true);

        assertFalse(battlezone.chooseDiscardedCrew(mario,housingUnit0));
        assertTrue(battlezone.chooseDiscardedCrew(mario,housingUnit1));
        assertTrue(battlezone.chooseDiscardedCrew(mario,housingUnit2));
        assertTrue(battlezone.chooseDiscardedCrew(mario,housingUnitAlienOrange));
        assertTrue(battlezone.chooseDiscardedCrew(mario,housingUnitAlienPurple));

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

        System.out.println(player.getSpaceship().getIdxShieldCount(0) + ", " + player.getSpaceship().getIdxShieldCount(1) + ", " + player.getSpaceship().getIdxShieldCount(2) + ", " + player.getSpaceship().getIdxShieldCount(3));

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
        assertTrue(bb1.placeComponent(1, 2, 0));

        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb1.placeComponent(1, 1, 1));

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        assertTrue(bb1.placeComponent(2, 3, 1));

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        assertTrue(bb1.placeComponent(3, 2, 1));

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb1.placeComponent(2, 1, 2));

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        assertTrue(bb1.placeComponent(3, 3, 0));

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-20s", 5 + i);
        }
        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE,2 , "img", new ArrayList<>());

        Projectile s1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile s2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile s3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile s4 = new Projectile(ProjectileSize.SMALL, 3);

        // up
        assertFalse(battlezone.penaltyShot(game1, p1, s1, 2));
        assertFalse(battlezone.penaltyShot(game1, p1, s1, 4));
        assertFalse(battlezone.penaltyShot(game1, p1, s1, 5));
        assertTrue(battlezone.penaltyShot(game1, p1, s1, 6));
        assertTrue(battlezone.penaltyShot(game1, p1, s1, 8));
        assertFalse(battlezone.penaltyShot(game1, p1, s1, 9));
        assertFalse(battlezone.penaltyShot(game1, p1, s1, 10));

        // bottom
        assertFalse(battlezone.penaltyShot(game1, p1, s3, 2));
        assertFalse(battlezone.penaltyShot(game1, p1, s3, 4));
        assertFalse(battlezone.penaltyShot(game1, p1, s3, 5));
        assertTrue(battlezone.penaltyShot(game1, p1, s3, 6));
        assertTrue(battlezone.penaltyShot(game1, p1, s3, 8));
        assertFalse(battlezone.penaltyShot(game1, p1, s3, 9));
        assertFalse(battlezone.penaltyShot(game1, p1, s3, 10));

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-20s", 5 + i);
        }
        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();

        // right
        assertFalse(battlezone.penaltyShot(game1, p1, s2, 3));
        assertFalse(battlezone.penaltyShot(game1, p1, s2, 5));
        assertTrue(battlezone.penaltyShot(game1, p1, s2, 6));
        assertTrue(battlezone.penaltyShot(game1, p1, s2, 8));
        assertFalse(battlezone.penaltyShot(game1, p1, s2, 9));
        assertFalse(battlezone.penaltyShot(game1, p1, s2, 10));

        // left
        assertFalse(battlezone.penaltyShot(game1, p1, s4, 3));
        assertFalse(battlezone.penaltyShot(game1, p1, s4, 5));
        assertTrue(battlezone.penaltyShot(game1, p1, s4, 7));
        assertFalse(battlezone.penaltyShot(game1, p1, s4, 8));
        assertFalse(battlezone.penaltyShot(game1, p1, s4, 9));
        assertFalse(battlezone.penaltyShot(game1, p1, s4, 10));

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-20s", 5 + i);
        }
        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();



        // check spaceShip lv2
        System.out.println("Check spaceship lv2");

        Game game2 = new Game(1, 3, 2);
        Player p2 = new Player("a", 1, 2);


        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        bb2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb2.placeComponent(1, 3, 0));

        bb2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb2.placeComponent(1, 2, 1));

        bb2.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        assertTrue(bb2.placeComponent(3, 3, 1));

        bb2.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        assertTrue(bb2.placeComponent(3, 2, 1));

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb2.placeComponent(2, 4, 1));

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        assertTrue(bb2.placeComponent(3, 4, 0));

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        assertTrue(bb2.placeComponent(3, 1, 0));

        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        assertTrue(bb2.placeComponent(3, 5, 0));

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 7; i++) {
            System.out.printf("%-20s", 4 + i);
        }
        for (int i = 0; i < bb2.getSpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < bb2.getSpaceshipMatrix()[0].length; j++) {
                String value = (bb2.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb2.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb2.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();

        Battlezone battlezone2 = new Battlezone(CardType.BATTLEZONE, 1, "img", new ArrayList<>());

        Projectile ss1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile ss2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile ss3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile ss4 = new Projectile(ProjectileSize.SMALL, 3);

        // up
        assertFalse(battlezone2.penaltyShot(game2, p2, ss1, 2));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss1, 4));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss1, 5));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss1, 6));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss1, 8));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss1, 10));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss1, 12));

        // bottom
        assertFalse(battlezone2.penaltyShot(game2, p2, ss3, 2));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss3, 4));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss3, 5));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss3, 6));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss3, 9));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss3, 10));

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 7; i++) {
            System.out.printf("%-20s", 4 + i);
        }
        for (int i = 0; i < bb2.getSpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < bb2.getSpaceshipMatrix()[0].length; j++) {
                String value = (bb2.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb2.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb2.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();

        // right
        assertFalse(battlezone2.penaltyShot(game2, p2, ss2, 3));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss2, 5));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss2, 6));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss2, 8));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss2, 9));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss2, 10));

        // left
        assertFalse(battlezone2.penaltyShot(game2, p2, ss4, 3));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss4, 5));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss4, 7));
        assertTrue(battlezone2.penaltyShot(game2, p2, ss4, 8));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss4, 9));
        assertFalse(battlezone2.penaltyShot(game2, p2, ss4, 10));

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 7; i++) {
            System.out.printf("%-20s", 4 + i);
        }
        for (int i = 0; i < bb2.getSpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < bb2.getSpaceshipMatrix()[0].length; j++) {
                String value = (bb2.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb2.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb2.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();
    }

    @Test
    void chooseDiscardedBox() {
        Player p = new Player("a", 1, 1);
        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 1, "img", new ArrayList<>());

        BoxStorage bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1,1,1,1}, "", 3, false);
        bs1.addBox(new Box(BoxType.YELLOW, 3),2);
        p.getSpaceship().addBoxCount(1, BoxType.YELLOW);
        bs1.addBox(new Box(BoxType.GREEN, 2),1);
        p.getSpaceship().addBoxCount(1, BoxType.GREEN);

        for (int i = 0; i < bs1.getCapacity(); i++) {
            if(bs1.getBoxStorage()[i] == null)
                System.out.print("NULL, ");
            else
                System.out.print(bs1.getBoxStorage()[i].getValue() + ", ");
        }
        System.out.println();

        assertFalse(battlezone.chooseDiscardedBox(p, bs1, 0));
        assertFalse(battlezone.chooseDiscardedBox(p, bs1, 1));
        assertNotEquals(null, (bs1.getBoxStorage()[1]));
        assertTrue(battlezone.chooseDiscardedBox(p, bs1, 2));
        assertNull(bs1.getBoxStorage()[2]);

        for (int i = 0; i < bs1.getCapacity(); i++) {
            if(bs1.getBoxStorage()[i] == null)
                System.out.print("NULL, ");
            else
                System.out.print(bs1.getBoxStorage()[i].getValue() + ", ");
        }
        System.out.println();


        BoxStorage bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "", 3, true);

        bs2.addBox(new Box(BoxType.GREEN, 2),0);
        bs2.addBox(new Box(BoxType.YELLOW, 3),2);

        for (int i = 0; i < bs2.getCapacity(); i++) {
            if(bs2.getBoxStorage()[i] == null)
                System.out.print("NULL, ");
            else
                System.out.print(bs2.getBoxStorage()[i].getValue() + ", ");
        }
        System.out.println();

        // todo: waiting for increment of counters in spaceship
    }

    @Test
    void chooseDiscardedBattery() {

        Player mario = new Player("mario",0,2);

        Battlezone battlezone = new Battlezone(CardType.BATTLEZONE, 2, "img", new ArrayList<>());
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(2);

        // Returns false if component is not a Housing Unit
        assertFalse(battlezone.chooseDiscardedBattery((BatteryStorage) notBattery,mario));

        // Removes one battery member from the Housing Unit
        assertTrue(battlezone.chooseDiscardedBattery(battery,mario));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(battlezone.chooseDiscardedBattery(battery,mario));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(battlezone.chooseDiscardedBattery(battery,mario));
        assertEquals(0, battery.getItemsCount());
    }
}