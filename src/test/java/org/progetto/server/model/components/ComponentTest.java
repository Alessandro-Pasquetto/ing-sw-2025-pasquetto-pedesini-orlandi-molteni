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
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        assertEquals(ComponentType.STRUCTURAL_UNIT, component.getType());
    }

    @Test
    void getConnections() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        assertArrayEquals(new int[]{1, 0, 1, 0}, component.getConnections());
    }

    @Test
    void getRotation() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        assertEquals(0, component.getRotation());

        component.setRotation(2);
        assertEquals(2, component.getRotation());
    }

    @Test
    void isHidden() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        assertFalse(component.isHidden());

        component.setHidden(true);
        assertTrue(component.isHidden());
    }

    @Test
    void isPlaced() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        assertFalse(component.isPlaced());

        component.setPlaced(true);
        assertTrue(component.isPlaced());
    }

    @Test
    void getImgSrc() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        assertEquals("imgSrc", component.getImgSrc());
    }

    @Test
    void getX_coordinate() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        component.setX(2);

        assertEquals(2, component.getX());
    }

    @Test
    void getY_coordinate() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        component.setY(3);

        assertEquals(3, component.getY());
    }

    @Test
    void setRotation() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        // Standard rotation
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgSrc"));
        buildingBoard.getHandComponent().setRotation(3);

        assertEquals(3, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{1, 2, 3, 0}, buildingBoard.getHandComponent().getConnections());

        // Param greater than 3
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgSrc"));
        buildingBoard.getHandComponent().setRotation(4);

        assertEquals(0, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{0, 1, 2, 3}, buildingBoard.getHandComponent().getConnections());

        // Param fewer than 0
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 2, 3}, "imgSrc"));
        buildingBoard.getHandComponent().setRotation(4);

        assertEquals(0, buildingBoard.getHandComponent().getRotation());
        assertArrayEquals(new int[]{0, 1, 2, 3}, buildingBoard.getHandComponent().getConnections());
    }

    @Test
    void setHidden() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        component.setHidden(true);
        assertTrue(component.isHidden());

        component.setHidden(false);
        assertFalse(component.isHidden());
    }

    @Test
    void setPlaced() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        component.setPlaced(true);
        assertTrue(component.isPlaced());

        component.setPlaced(false);
        assertFalse(component.isPlaced());
    }

    @Test
    void setX_coordinate() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        component.setX(5);

        assertEquals(5, component.getX());
    }

    @Test
    void setY_coordinate() {
        Component component = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 0, 1, 0}, "imgSrc");

        component.setY(7);

        assertEquals(7, component.getY());
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