package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PiratesTest {

    @Test
    void getFirePowerRequired() {
        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, new ArrayList<>());
        assertEquals(5, pirates.getFirePowerRequired());
    }

    @Test
    void getPenaltyDays() {
        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, new ArrayList<>());
        assertEquals(-3, pirates.getPenaltyDays());
    }

    @Test
    void getRewardCredits() {
        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, new ArrayList<>());
        assertEquals(3, pirates.getRewardCredits());
    }

    @Test
    void getPenaltyShots() {
        ArrayList<Projectile> projectiles = new ArrayList<>();
        projectiles.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles.add(new Projectile(ProjectileSize.SMALL, 3));
        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, projectiles);
        assertEquals(projectiles, pirates.getPenaltyShots());
    }

    @Test
    void checkShields() {
        Game game = new Game(0, 3, 2);
        Player player = new Player("a");

        game.addPlayer(player);
        game.initPlayersSpaceship();

        player.getSpaceship().addRightDownShieldCount(2);

        Projectile p1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile p2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile p3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile p4 = new Projectile(ProjectileSize.SMALL, 3);

        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, new ArrayList<>());

        assertFalse(pirates.checkShields(player, p1));
        assertTrue(pirates.checkShields(player, p2));
        assertTrue(pirates.checkShields(player, p3));
        assertFalse(pirates.checkShields(player, p4));
    }

    @Test
    void chooseDiscardedBattery() {
        Game game = new Game(0, 3, 2);

        Player mario = new Player("mario");

        game.addPlayer(mario);
        game.initPlayersSpaceship();

        ArrayList<Projectile> projectiles = new ArrayList<>();
        projectiles.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles.add(new Projectile(ProjectileSize.SMALL, 3));
        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, projectiles);
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(mario.getSpaceship(), 2);

        // Returns false if component is not a Housing Unit
        assertFalse(pirates.chooseDiscardedBattery(mario.getSpaceship(), (BatteryStorage) notBattery));

        // Removes one battery member from the Housing Unit
        assertTrue(pirates.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(pirates.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(pirates.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(0, battery.getItemsCount());
    }

    @Test
    void penaltyShot() {
        Game game1 = new Game(0, 3, 1);
        Game game2 = new Game(1, 3, 2);
        Board board1 = game1.getBoard();
        Board board2 = game2.getBoard();

        Player p1 = new Player("a");
        Player p2 = new Player("a");

        game1.addPlayer(p1);
        game1.addPlayer(p2);
        game1.initPlayersSpaceship();

        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();
        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(2, 1, 0);

        bb1.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(1, 1, 1);

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb1.placeComponent(3, 2, 1);

        bb1.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb1.placeComponent(2, 3, 1);

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb1.placeComponent(1, 2, 2);

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        bb1.placeComponent(3, 3, 0);

//        System.out.println();
//        System.out.printf("%-20s", "-");
//        for (int i = 0; i < 5; i++) {
//            System.out.printf("%-20s", 5 + i);
//        }
//        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
//            System.out.println();
//            System.out.printf("%-20s", 5 + i);
//            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
//                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
//                System.out.printf("%-20s", value);
//            }
//        }
//        System.out.println();

        Pirates pirates = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, new ArrayList<>());

        Projectile s1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile s2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile s3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile s4 = new Projectile(ProjectileSize.SMALL, 3);

        // up
        assertNull(pirates.penaltyShot(game1, p1, s1, 2));
        assertNull(pirates.penaltyShot(game1, p1, s1, 4));
        assertNull(pirates.penaltyShot(game1, p1, s1, 5));
        assertNotNull(pirates.penaltyShot(game1, p1, s1, 6));
        assertNotNull(pirates.penaltyShot(game1, p1, s1, 8));
        assertNull(pirates.penaltyShot(game1, p1, s1, 9));
        assertNull(pirates.penaltyShot(game1, p1, s1, 10));

        // bottom
        assertNull(pirates.penaltyShot(game1, p1, s3, 2));
        assertNull(pirates.penaltyShot(game1, p1, s3, 4));
        assertNull(pirates.penaltyShot(game1, p1, s3, 5));
        assertNotNull(pirates.penaltyShot(game1, p1, s3, 6));
        assertNotNull(pirates.penaltyShot(game1, p1, s3, 8));
        assertNull(pirates.penaltyShot(game1, p1, s3, 9));
        assertNull(pirates.penaltyShot(game1, p1, s3, 10));

//        System.out.println();
//        System.out.printf("%-20s", "-");
//        for (int i = 0; i < 5; i++) {
//            System.out.printf("%-20s", 5 + i);
//        }
//        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
//            System.out.println();
//            System.out.printf("%-20s", 5 + i);
//            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
//                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
//                System.out.printf("%-20s", value);
//            }
//        }
//        System.out.println();

        // right
        assertNull(pirates.penaltyShot(game1, p1, s2, 3));
        assertNull(pirates.penaltyShot(game1, p1, s2, 5));
        assertNotNull(pirates.penaltyShot(game1, p1, s2, 6));
        assertNotNull(pirates.penaltyShot(game1, p1, s2, 8));
        assertNull(pirates.penaltyShot(game1, p1, s2, 9));
        assertNull(pirates.penaltyShot(game1, p1, s2, 10));

//        System.out.println();
//        System.out.printf("%-20s", "-");
//        for (int i = 0; i < 5; i++) {
//            System.out.printf("%-20s", 5 + i);
//        }
//        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
//            System.out.println();
//            System.out.printf("%-20s", 5 + i);
//            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
//                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
//                System.out.printf("%-20s", value);
//            }
//        }
//        System.out.println();

        // left
        assertNull(pirates.penaltyShot(game1, p1, s4, 3));
        assertNull(pirates.penaltyShot(game1, p1, s4, 5));
        assertNotNull(pirates.penaltyShot(game1, p1, s4, 7));
        assertNotNull(pirates.penaltyShot(game1, p1, s4, 8));
        assertNull(pirates.penaltyShot(game1, p1, s4, 9));
        assertNull(pirates.penaltyShot(game1, p1, s4, 10));

//        System.out.println();
//        System.out.printf("%-20s", "-");
//        for (int i = 0; i < 5; i++) {
//            System.out.printf("%-20s", 5 + i);
//        }
//        for (int i = 0; i < bb1.getSpaceshipMatrix().length; i++) {
//            System.out.println();
//            System.out.printf("%-20s", 5 + i);
//            for (int j = 0; j < bb1.getSpaceshipMatrix()[0].length; j++) {
//                String value = (bb1.getSpaceshipMatrix()[i][j] == null) ? "NULL" : bb1.getSpaceshipMatrix()[i][j].getType().toString() + "-" + bb1.getSpaceshipMatrix()[i][j].getRotation();
//                System.out.printf("%-20s", value);
//            }
//        }
//        System.out.println();
    }

    @Test
    void rewardPenalty() {
        ArrayList<Projectile> projectiles1 = new ArrayList<>();
        projectiles1.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles1.add(new Projectile(ProjectileSize.SMALL, 3));
        ArrayList<Projectile> projectiles2 = new ArrayList<>();
        projectiles2.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles2.add(new Projectile(ProjectileSize.SMALL, 3));

        Game game = new Game(0, 2, 2);
        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initPlayersSpaceship();

        Board board = new Board(1, 2);
        board.addTraveler(player1);
        board.addTraveler(player2);

        board.addTravelersOnTrack(1);

        Pirates pirates1 = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, projectiles1);
        Pirates pirates2 = new Pirates(CardType.PIRATES, 2, "imgPath", 6, -2, 4, projectiles2);

        pirates1.rewardPenalty(board, player1);

        //adds 3 credits to player1
        assertEquals(3, player1.getCredits());

        //moves player1 back 3
        assertEquals(0, player1.getPosition());

        pirates2.rewardPenalty(board, player2);

        //adds 2 credits to player2
        assertEquals(4, player2.getCredits());

        //moves player2 back 2
        assertEquals(0, player1.getPosition());
    }

    @Test
    void battleResult() {
        ArrayList<Projectile> projectiles1 = new ArrayList<>();
        projectiles1.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles1.add(new Projectile(ProjectileSize.SMALL, 3));
        ArrayList<Projectile> projectiles2 = new ArrayList<>();
        projectiles2.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles2.add(new Projectile(ProjectileSize.SMALL, 3));

        Game game = new Game(0, 2, 2);
        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initPlayersSpaceship();

        Pirates pirates1 = new Pirates(CardType.PIRATES, 2, "imgPath", 5, -3, 3, projectiles1);
        Pirates pirates2 = new Pirates(CardType.PIRATES, 2, "imgPath", 6, -2, 4, projectiles2);

        //compares a power equal to the one required
        assertEquals(0, pirates1.battleResult(player1, 5));

        //compares a lower power than required
        assertEquals(-1, pirates2.battleResult(player1, 5));

        //compares a higher power than required
        assertEquals(1, pirates1.battleResult(player2, 8));
    }
}