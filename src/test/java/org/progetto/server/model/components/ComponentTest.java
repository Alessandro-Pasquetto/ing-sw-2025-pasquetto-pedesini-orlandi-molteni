package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Spaceship;

import static org.junit.jupiter.api.Assertions.*;
class ComponentTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getType() {
    }

    @Test
    void getConnections() {
    }

    @Test
    void getRotation() {
    }

    @Test
    void isHidden() {
    }

    @Test
    void isPlaced() {
    }

    @Test
    void getImgSrc() {
    }

    @Test
    void getX_coordinate() {
    }

    @Test
    void getY_coordinate() {
    }

    @Test
    void setRotation() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        // Standard rotation
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(3);

        assertEquals(3, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{1, 2, 3, 0}, buildingBoard.getHandComponent().getConnections());

        // Param greater than 3
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(4);

        assertEquals(0, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{0, 1, 2, 3}, buildingBoard.getHandComponent().getConnections());

        // Param fewer than 0
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(4);

        assertEquals(0, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{0, 1, 2, 3}, buildingBoard.getHandComponent().getConnections());
    }

    @Test
    void setHidden() {
    }

    @Test
    void setPlaced() {
    }

    @Test
    void setX_coordinate() {
    }

    @Test
    void setY_coordinate() {
    }

    @Test
    void rotate() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        // Rotate one time
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.getHandComponent().rotate();

        assertEquals(1, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{3, 0, 1, 2}, buildingBoard.getHandComponent().getConnections());
    }
}