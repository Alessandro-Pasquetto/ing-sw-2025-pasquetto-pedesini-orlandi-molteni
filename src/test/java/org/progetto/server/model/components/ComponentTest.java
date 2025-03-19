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

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{2, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(3);

        for (int i = 0; i < 4; i++) {
            System.out.print(buildingBoard.getHandComponent().getConnections()[i] + " ");
        }

        assertArrayEquals(new int[]{1, 1, 1, 2}, buildingBoard.getHandComponent().getConnections());
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
    }
}