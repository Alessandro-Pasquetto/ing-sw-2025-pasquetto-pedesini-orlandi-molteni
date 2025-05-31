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
        ArrayList<Projectile> projectiles = new ArrayList<>();
        projectiles.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles.add(new Projectile(ProjectileSize.SMALL, 3));
        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN, 2, "img", projectiles);
        assertEquals(projectiles, meteorsRain.getMeteors());
    }

    @Test
    void checkImpactComponent() {
        Game game = new Game(0, 3, 1);
        Board board = game.getBoard();

        Player player = new Player("gino");

        game.addPlayer(player);
        game.initPlayersSpaceship();

        BuildingBoard bb = player.getSpaceship().getBuildingBoard();

        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb.placeComponent(2, 1, 0);

        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        bb.placeComponent(1, 1, 1);

        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb.placeComponent(3, 2, 1);

        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        bb.placeComponent(2, 3, 1);

        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        bb.placeComponent(1, 2, 2);

        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        bb.placeComponent(3, 3, 0);


        MeteorsRain meteorsRain = new MeteorsRain(CardType.METEORSRAIN, 2, "img", new ArrayList<>());

        Projectile s1 = new Projectile(ProjectileSize.BIG, 0);
        Projectile s2 = new Projectile(ProjectileSize.SMALL, 1);
        Projectile s3 = new Projectile(ProjectileSize.BIG, 2);
        Projectile s4 = new Projectile(ProjectileSize.SMALL, 3);

        Component[][] spaceshipMatrix = bb.getSpaceshipMatrixCopy();

        // up
        assertNull(meteorsRain.checkImpactComponent(game, player, s1, 2));
        assertNull(meteorsRain.checkImpactComponent(game, player, s1, 4));
        assertNull(meteorsRain.checkImpactComponent(game, player, s1, 5));
        assertEquals(spaceshipMatrix[1][1], meteorsRain.checkImpactComponent(game, player, s1, 6));
        assertEquals(spaceshipMatrix[2][3], meteorsRain.checkImpactComponent(game, player, s1, 8));
        assertNull(meteorsRain.checkImpactComponent(game, player, s1, 9));
        assertNull(meteorsRain.checkImpactComponent(game, player, s1, 10));

        // bottom
        assertNull(meteorsRain.checkImpactComponent(game, player, s3, 2));
        assertNull(meteorsRain.checkImpactComponent(game, player, s3, 4));
        assertNull(meteorsRain.checkImpactComponent(game, player, s3, 5));
        assertEquals(spaceshipMatrix[2][1], meteorsRain.checkImpactComponent(game, player, s3, 6));
        assertEquals(spaceshipMatrix[3][3], meteorsRain.checkImpactComponent(game, player, s3, 8));
        assertNull(meteorsRain.checkImpactComponent(game, player, s3, 9));
        assertNull(meteorsRain.checkImpactComponent(game, player, s3, 10));

        // right
        assertNull(meteorsRain.checkImpactComponent(game, player, s2, 3));
        assertNull(meteorsRain.checkImpactComponent(game, player, s2, 5));
        assertEquals(spaceshipMatrix[1][2], meteorsRain.checkImpactComponent(game, player, s2, 6));
        assertEquals(spaceshipMatrix[3][3], meteorsRain.checkImpactComponent(game, player, s2, 8));
        assertNull(meteorsRain.checkImpactComponent(game, player, s2, 9));
        assertNull(meteorsRain.checkImpactComponent(game, player, s2, 10));

        //left
        assertNull(meteorsRain.checkImpactComponent(game, player, s4, 3));
        assertNull(meteorsRain.checkImpactComponent(game, player, s4, 5));
        assertEquals(spaceshipMatrix[2][1], meteorsRain.checkImpactComponent(game, player, s4, 7));
        assertEquals(spaceshipMatrix[3][2], meteorsRain.checkImpactComponent(game, player, s4, 8));
        assertNull(meteorsRain.checkImpactComponent(game, player, s4, 9));
        assertNull(meteorsRain.checkImpactComponent(game, player, s4, 10));
    }

    @Test
    void checkShields() {
        Game game = new Game(0, 3, 1);

        Player player = new Player("gino");

        game.addPlayer(player);
        game.initPlayersSpaceship();

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
}