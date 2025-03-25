package org.progetto.server.model.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MeteorsRainTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getMeteors() {
    }

    @Test
    void checkImpactComponent() {
        Game game = new Game(0, 3, 1);
        Board board = game.getBoard();

        Player player = new Player("gino", 0, 1);

        BuildingBoard bb = player.getSpaceship().getBuildingBoard();

        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb.placeComponent(1, 2, 0));

        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb.placeComponent(1, 1, 1));

        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        assertTrue(bb.placeComponent(2, 3, 1));

        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        assertTrue(bb.placeComponent(3, 2, 1));

        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        assertTrue(bb.placeComponent(2, 1, 2));

        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        assertTrue(bb.placeComponent(3, 3, 0));


        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN, 2, "img", new ArrayList<>());

        Projectile s1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile s2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile s3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile s4 = new Projectile(ProjectileSize.SMALL, 3);

        Component[][] spaceshipMatrix = bb.getSpaceshipMatrix();

        // up
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s1, 2));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s1, 4));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s1, 5));
        assertEquals(spaceshipMatrix[1][1], meteorsRain.checkImpactComponent(game, player, s1, 6));
        assertEquals(spaceshipMatrix[2][3], meteorsRain.checkImpactComponent(game, player, s1, 8));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s1, 9));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s1, 10));

        // bottom
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s3, 2));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s3, 4));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s3, 5));
        assertEquals(spaceshipMatrix[2][1], meteorsRain.checkImpactComponent(game, player, s3, 6));
        assertEquals(spaceshipMatrix[3][3], meteorsRain.checkImpactComponent(game, player, s3, 8));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s3, 9));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s3, 10));

        // right
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s2, 3));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s2, 5));
        assertEquals(spaceshipMatrix[1][2], meteorsRain.checkImpactComponent(game, player, s2, 6));
        assertEquals(spaceshipMatrix[3][3], meteorsRain.checkImpactComponent(game, player, s2, 8));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s2, 9));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s2, 10));

        //left
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s4, 3));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s4, 5));
        assertEquals(spaceshipMatrix[2][1], meteorsRain.checkImpactComponent(game, player, s4, 7));
        assertEquals(spaceshipMatrix[3][2], meteorsRain.checkImpactComponent(game, player, s4, 8));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s4, 9));
        assertEquals(null, meteorsRain.checkImpactComponent(game, player, s4, 10));
    }

    @Test
    void checkShields() {
        Player player = new Player("gino", 0, 1);
        player.getSpaceship().addRightDownShieldCount(2);

        Projectile p1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile p2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile p3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile p4 = new Projectile(ProjectileSize.SMALL, 3);

        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN, 2, "img", new ArrayList<>());

        assertFalse(meteorsRain.checkShields(player, p1));
        assertTrue(meteorsRain.checkShields(player, p2));
        assertTrue(meteorsRain.checkShields(player, p3));
        assertFalse(meteorsRain.checkShields(player, p4));
    }

    @Test
    void chooseDiscardedBattery() {

        Player mario = new Player("mario", 0, 1);

        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN, 2, "img", new ArrayList<>());

        BatteryStorage notBattery = new BatteryStorage(ComponentType.STRUCTURAL_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(mario.getSpaceship(),2);

        // Returns false if component is not a Housing Unit
        assertFalse(meteorsRain.chooseDiscardedBattery(mario.getSpaceship(),(BatteryStorage) notBattery));

        // Removes one battery member from the Housing Unit
        assertTrue(meteorsRain.chooseDiscardedBattery(mario.getSpaceship(),battery));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(meteorsRain.chooseDiscardedBattery(mario.getSpaceship(),battery));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(meteorsRain.chooseDiscardedBattery(mario.getSpaceship(),battery));
        assertEquals(0, battery.getItemsCount());
    }
}