package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.progetto.server.model.components.BoxType.*;

class SpaceshipTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getShipComponentCount() {
    }

    @Test
    void getDestroyedCount() {
    }

    @Test
    void getCrewCount() {
    }

    @Test
    void getBoxesValue() {
    }

    @Test
    void getBatteriesCount() {
    }

    @Test
    void getExposedConnectorsCount() {
    }

    @Test
    void getAlienPurple() {
    }

    @Test
    void getAlienOrange() {
    }

    @Test
    void getNormalShootingPower() {
    }

    @Test
    void getDoubleCannonCount() {
    }

    @Test
    void getDoubleEngineCount() {
    }

    @Test
    void getNormalEnginePower() {
    }

    @Test
    void getIdxShieldCount() {
    }

    @Test
    void getBoxCounts() {
    }

    @Test
    void getBuildingBoard() {
    }

    @Test
    void setExposedConnectorsCount() {
    }

    @Test
    void setAlienPurple() {
    }

    @Test
    void setAlienOrange() {
    }

    @Test
    void addComponentShipCount() {
        Spaceship spaceship = new Spaceship(1,0);
        spaceship.addComponentsShipCount(1);
        assertEquals(2, spaceship.getShipComponentsCount());

        spaceship.addComponentsShipCount(1);
        assertEquals(3, spaceship.getShipComponentsCount());

        Spaceship spaceship2 = new Spaceship(2,3);
        spaceship2.addComponentsShipCount(1);
        assertEquals(2, spaceship2.getShipComponentsCount());

        spaceship2.addComponentsShipCount(3);
        assertEquals(5, spaceship2.getShipComponentsCount());
    }

    @Test
    void addDestroyedCount() {
        Spaceship spaceship = new Spaceship(1,0);
        spaceship.addDestroyedCount(1);
        assertEquals(1, spaceship.getDestroyedCount());

        spaceship.addDestroyedCount(1);
        assertEquals(2, spaceship.getDestroyedCount());

        Spaceship spaceship2 = new Spaceship(2,3);
        spaceship2.addDestroyedCount(1);
        assertEquals(1, spaceship2.getDestroyedCount());

        spaceship2.addDestroyedCount(3);
        assertEquals(4, spaceship2.getDestroyedCount());
    }

    @Test
    void addCrewCount() {
        Spaceship spaceship = new Spaceship(1,0);
        spaceship.addCrewCount(1);
        assertEquals(1, spaceship.getCrewCount());

        spaceship.addCrewCount(1);
        assertEquals(2, spaceship.getCrewCount());

        Spaceship spaceship2 = new Spaceship(2,3);
        spaceship2.addCrewCount(1);
        assertEquals(1, spaceship2.getCrewCount());

        spaceship2.addCrewCount(3);
        assertEquals(4, spaceship2.getCrewCount());
    }

    @Test
    void addBatteriesCount() {
        Spaceship spaceship = new Spaceship(1,0);
        spaceship.addBatteriesCount(2);
        assertEquals(2, spaceship.getBatteriesCount());

        spaceship.addBatteriesCount(1);
        assertEquals(3, spaceship.getBatteriesCount());

        Spaceship spaceship2 = new Spaceship(2,3);
        spaceship2.addBatteriesCount(2);
        assertEquals(2, spaceship2.getBatteriesCount());

        spaceship2.addBatteriesCount(3);
        assertEquals(5, spaceship2.getBatteriesCount());
    }

    @Test
    void addNormalShootingPower() {
        Spaceship spaceship = new Spaceship(1,0);
        spaceship.addNormalShootingPower(2);
        assertEquals(2, spaceship.getNormalShootingPower());

        spaceship.addNormalShootingPower(1.5f);
        assertEquals(3.5, spaceship.getNormalShootingPower());

        spaceship.addNormalShootingPower(-2);
        assertEquals(1.5, spaceship.getNormalShootingPower());

        Spaceship spaceship2 = new Spaceship(2,3);
        spaceship2.addNormalShootingPower(2);
        assertEquals(2, spaceship2.getNormalShootingPower());

        spaceship2.addNormalShootingPower(3.5f);
        assertEquals(5.5, spaceship2.getNormalShootingPower());

        spaceship2.addNormalShootingPower(-3.5f);
        assertEquals(2, spaceship2.getNormalShootingPower());
    }

    @Test
    void addDoubleCannonCount() {
        Spaceship spaceship = new Spaceship(1,1);
        spaceship.addDoubleCannonCount(1);
        assertEquals(1, spaceship.getDoubleCannonCount());

        spaceship.addDoubleCannonCount(1);
        assertEquals(2, spaceship.getDoubleCannonCount());

        Spaceship spaceship2 = new Spaceship(2,2);
        spaceship2.addDoubleCannonCount(1);
        assertEquals(1, spaceship2.getDoubleCannonCount());

        spaceship2.addDoubleCannonCount(1);
        assertEquals(2, spaceship2.getDoubleCannonCount());
    }

    @Test
    void addNormalEnginePower() {
        Spaceship spaceship = new Spaceship(1,0);
        spaceship.addNormalEnginePower(2);
        assertEquals(2, spaceship.getNormalEnginePower());

        spaceship.addNormalEnginePower(1.5f);
        assertEquals(3.5, spaceship.getNormalEnginePower());

        spaceship.addNormalEnginePower(-2);
        assertEquals(1.5, spaceship.getNormalEnginePower());

        Spaceship spaceship2 = new Spaceship(2,3);
        spaceship2.addNormalEnginePower(2);
        assertEquals(2, spaceship2.getNormalEnginePower());

        spaceship2.addNormalEnginePower(3.5f);
        assertEquals(5.5, spaceship2.getNormalEnginePower());

        spaceship2.addNormalEnginePower(-3.5f);
        assertEquals(2, spaceship2.getNormalEnginePower());
    }

    @Test
    void addDoubleEngineCount() {
        Spaceship spaceship = new Spaceship(1,1);
        spaceship.addDoubleEngineCount(1);
        assertEquals(1, spaceship.getDoubleEngineCount());

        spaceship.addDoubleEngineCount(1);
        assertEquals(2, spaceship.getDoubleEngineCount());

        Spaceship spaceship2 = new Spaceship(2,0);
        spaceship2.addDoubleEngineCount(1);
        assertEquals(1, spaceship2.getDoubleEngineCount());

        spaceship2.addDoubleEngineCount(1);
        assertEquals(2, spaceship2.getDoubleEngineCount());
    }

    @Test
    void addLeftUpShieldCount() {
        Spaceship spaceship = new Spaceship(1,1);
        spaceship.addLeftUpShieldCount(1);
        assertEquals(1, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(1, spaceship.getIdxShieldCount(3));

        spaceship.addLeftUpShieldCount(1);
        assertEquals(2, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(2, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addUpRightShieldCount() {
        Spaceship spaceship = new Spaceship(2,2);
        spaceship.addUpRightShieldCount(1);
        assertEquals(1, spaceship.getIdxShieldCount(0));
        assertEquals(1, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));

        spaceship.addUpRightShieldCount(1);
        assertEquals(2, spaceship.getIdxShieldCount(0));
        assertEquals(2, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addRightDownShieldCount() {
        Spaceship spaceship = new Spaceship(1,3);
        spaceship.addRightDownShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(1, spaceship.getIdxShieldCount(1));
        assertEquals(1, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));

        spaceship.addRightDownShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(2, spaceship.getIdxShieldCount(1));
        assertEquals(2, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addDownLeftShieldCount() {
        Spaceship spaceship = new Spaceship(2,0);
        spaceship.addDownLeftShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(1, spaceship.getIdxShieldCount(2));
        assertEquals(1, spaceship.getIdxShieldCount(3));

        spaceship.addDownLeftShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(2, spaceship.getIdxShieldCount(2));
        assertEquals(2, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addBoxCount() {
        Spaceship spaceship = new Spaceship(1,2);
        spaceship.addBoxCount(1, GREEN);
        assertArrayEquals(new int[] {0, 0, 1, 0}, spaceship.getBoxCounts());

        spaceship.addBoxCount(1, RED);
        assertArrayEquals(new int[] {1, 0, 1, 0}, spaceship.getBoxCounts());

        spaceship.addBoxCount(1, GREEN);
        assertArrayEquals(new int[] {1, 0, 2, 0}, spaceship.getBoxCounts());

        spaceship.addBoxCount(1, YELLOW);
        assertArrayEquals(new int[] {1, 1, 2, 0}, spaceship.getBoxCounts());

        spaceship.addBoxCount(1, BLUE);
        assertArrayEquals(new int[] {1, 1, 2, 1}, spaceship.getBoxCounts());

        Spaceship spaceship2 = new Spaceship(2,1);
        spaceship2.addBoxCount(1, YELLOW);
        assertArrayEquals(new int[] {0, 1, 0, 0}, spaceship2.getBoxCounts());

        spaceship2.addBoxCount(1, YELLOW);
        assertArrayEquals(new int[] {0, 2, 0, 0}, spaceship2.getBoxCounts());

        spaceship2.addBoxCount(1, BLUE);
        assertArrayEquals(new int[] {0, 2, 0, 1}, spaceship2.getBoxCounts());

        spaceship.addBoxCount(1, GREEN);
        assertArrayEquals(new int[] {0, 2, 1, 1}, spaceship.getBoxCounts());

        spaceship.addBoxCount(1, RED);
        assertArrayEquals(new int[] {1, 2, 1, 1}, spaceship.getBoxCounts());
    }
}