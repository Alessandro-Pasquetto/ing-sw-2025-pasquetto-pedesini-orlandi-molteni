package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import static org.junit.jupiter.api.Assertions.*;

class SpaceshipTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getShipComponentCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(1, spaceship.getShipComponentsCount());

        spaceship.addComponentsShipCount(2);
        assertEquals(3, spaceship.getShipComponentsCount());
    }

    @Test
    void getDestroyedCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getDestroyedCount());

        spaceship.addDestroyedCount(1);
        assertEquals(1, spaceship.getDestroyedCount());
    }

    @Test
    void getCrewCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getCrewCount());

        spaceship.addCrewCount(2);
        assertEquals(2, spaceship.getCrewCount());
    }

    @Test
    void getBoxesValue() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getBoxesValue());

        spaceship.addBoxCount(1, Box.RED);
        spaceship.addBoxCount(1, Box.YELLOW);
        spaceship.addBoxCount(1, Box.GREEN);
        spaceship.addBoxCount(1, Box.BLUE);
        assertEquals(10, spaceship.getBoxesValue());
    }

    @Test
    void getBatteriesCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getBatteriesCount());

        spaceship.addBatteriesCount(3);
        assertEquals(3, spaceship.getBatteriesCount());
    }

    @Test
    void getExposedConnectorsCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getExposedConnectorsCount());

        spaceship.setExposedConnectorsCount(2);
        assertEquals(2, spaceship.getExposedConnectorsCount());
    }

    @Test
    void getAlienPurple() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertFalse(spaceship.getAlienPurple());

        spaceship.setAlienPurple(true);
        assertTrue(spaceship.getAlienPurple());
    }

    @Test
    void getAlienOrange() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertFalse(spaceship.getAlienOrange());

        spaceship.setAlienOrange(true);
        assertTrue(spaceship.getAlienOrange());
    }

    @Test
    void getNormalShootingPower() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getNormalShootingPower());

        spaceship.addNormalShootingPower(2.5f);
        assertEquals(2.5f, spaceship.getNormalShootingPower());
    }

    @Test
    void getFullDoubleCannonCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getFullDoubleCannonCount());

        spaceship.addFullDoubleCannonCount(1);
        assertEquals(1, spaceship.getFullDoubleCannonCount());
    }

    @Test
    void getHalfDoubleCannonCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getHalfDoubleCannonCount());

        spaceship.addHalfDoubleCannonCount(1);
        assertEquals(1, spaceship.getHalfDoubleCannonCount());
    }

    @Test
    void getDoubleEngineCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getDoubleEngineCount());

        spaceship.addDoubleEngineCount(1);
        assertEquals(1, spaceship.getDoubleEngineCount());
    }

    @Test
    void getNormalEnginePower() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getNormalEnginePower());

        spaceship.addNormalEnginePower(4);
        assertEquals(4, spaceship.getNormalEnginePower());
    }

    @Test
    void getIdxShieldCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertEquals(0, spaceship.getIdxShieldCount(0)); // UP
        assertEquals(0, spaceship.getIdxShieldCount(1)); // RIGHT
        assertEquals(0, spaceship.getIdxShieldCount(2)); // DOWN
        assertEquals(0, spaceship.getIdxShieldCount(3)); // LEFT

        spaceship.addLeftUpShieldCount(1);
        assertEquals(1, spaceship.getIdxShieldCount(0)); // UP
        assertEquals(0, spaceship.getIdxShieldCount(1)); // RIGHT
        assertEquals(0, spaceship.getIdxShieldCount(2)); // DOWN
        assertEquals(1, spaceship.getIdxShieldCount(3)); // LEFT
    }

    @Test
    void getBoxCounts() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertArrayEquals(new int[]{0, 0, 0, 0}, spaceship.getBoxCounts());

        spaceship.addBoxCount(1, Box.RED);
        spaceship.addBoxCount(2, Box.BLUE);
        assertArrayEquals(new int[]{1, 0, 0, 2}, spaceship.getBoxCounts());
    }

    @Test
    void getBuildingBoard() {
        Spaceship spaceship = new Spaceship(1, 0);

        assertNotNull(spaceship.getBuildingBoard());
    }

    @Test
    void setExposedConnectorsCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        spaceship.setExposedConnectorsCount(3);
        assertEquals(3, spaceship.getExposedConnectorsCount());

        spaceship.setExposedConnectorsCount(5);
        assertEquals(5, spaceship.getExposedConnectorsCount());
    }

    @Test
    void setAlienPurple() {
        Spaceship spaceship = new Spaceship(1, 0);

        spaceship.setAlienPurple(true);
        assertTrue(spaceship.getAlienPurple());

        spaceship.setAlienPurple(false);
        assertFalse(spaceship.getAlienPurple());
    }

    @Test
    void setAlienOrange() {
        Spaceship spaceship = new Spaceship(1, 0);

        spaceship.setAlienOrange(true);
        assertTrue(spaceship.getAlienOrange());

        spaceship.setAlienOrange(false);
        assertFalse(spaceship.getAlienOrange());
    }

    @Test
    void addComponentShipCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        //adds one component to ComponentCount in level 1 ship
        spaceship.addComponentsShipCount(1);
        assertEquals(2, spaceship.getShipComponentsCount());

        //adds one component to ComponentCount in level 1 ship
        spaceship.addComponentsShipCount(1);
        assertEquals(3, spaceship.getShipComponentsCount());

        Spaceship spaceship2 = new Spaceship(2, 3);

        //adds one component to ComponentCount in level 2 ship
        spaceship2.addComponentsShipCount(1);
        assertEquals(2, spaceship2.getShipComponentsCount());

        //adds three components to ComponentCount in level 2 ship
        spaceship2.addComponentsShipCount(3);
        assertEquals(5, spaceship2.getShipComponentsCount());
    }

    @Test
    void addDestroyedCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        //adds one component to DestroyedCount in level 1 ship
        spaceship.addDestroyedCount(1);
        assertEquals(1, spaceship.getDestroyedCount());

        //adds one component to DestroyedCount in level 1 ship
        spaceship.addDestroyedCount(1);
        assertEquals(2, spaceship.getDestroyedCount());

        Spaceship spaceship2 = new Spaceship(2, 3);

        //adds one component to DestroyedCount in level 2 ship
        spaceship2.addDestroyedCount(1);
        assertEquals(1, spaceship2.getDestroyedCount());

        //adds three components to DestroyedCount in level 2 ship
        spaceship2.addDestroyedCount(3);
        assertEquals(4, spaceship2.getDestroyedCount());
    }

    @Test
    void addCrewCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        //adds one crew member to CrewCount in level 1 ship
        spaceship.addCrewCount(1);
        assertEquals(1, spaceship.getCrewCount());

        //adds one crew member to CrewCount in level 1 ship
        spaceship.addCrewCount(1);
        assertEquals(2, spaceship.getCrewCount());

        Spaceship spaceship2 = new Spaceship(2, 3);

        //adds one crew member to CrewCount in level 2 ship
        spaceship2.addCrewCount(1);
        assertEquals(1, spaceship2.getCrewCount());

        //adds three crew members to CrewCount in level 2 ship
        spaceship2.addCrewCount(3);
        assertEquals(4, spaceship2.getCrewCount());
    }

    @Test
    void addBatteriesCount() {
        Spaceship spaceship = new Spaceship(1, 0);

        //adds two batteries to BatteriesCount in level 1 ship
        spaceship.addBatteriesCount(2);
        assertEquals(2, spaceship.getBatteriesCount());

        //adds one battery to BatteriesCount in level 1 ship
        spaceship.addBatteriesCount(1);
        assertEquals(3, spaceship.getBatteriesCount());

        Spaceship spaceship2 = new Spaceship(2, 3);

        //adds two batteries to BatteriesCount in level 2 ship
        spaceship2.addBatteriesCount(2);
        assertEquals(2, spaceship2.getBatteriesCount());

        //adds three batteries to BatteriesCount in level 2 ship
        spaceship2.addBatteriesCount(3);
        assertEquals(5, spaceship2.getBatteriesCount());
    }

    @Test
    void addNormalShootingPower() {
        Spaceship spaceship = new Spaceship(1, 0);

        //adds two shooting power to NormalShootingPower in level 1 ship
        spaceship.addNormalShootingPower(2);
        assertEquals(2, spaceship.getNormalShootingPower());

        //adds one point five shooting power to NormalShootingPower in level 1 ship
        spaceship.addNormalShootingPower(1.5f);
        assertEquals(3.5, spaceship.getNormalShootingPower());

        //remove two shooting power from NormalShootingPower in level 1 ship
        spaceship.addNormalShootingPower(-2);
        assertEquals(1.5, spaceship.getNormalShootingPower());

        Spaceship spaceship2 = new Spaceship(2, 3);

        //adds two shooting power to NormalShootingPower in level 2 ship
        spaceship2.addNormalShootingPower(2);
        assertEquals(2, spaceship2.getNormalShootingPower());

        //adds three point five shooting power to NormalShootingPower in level 2 ship
        spaceship2.addNormalShootingPower(3.5f);
        assertEquals(5.5, spaceship2.getNormalShootingPower());

        //removes three point five shooting power to NormalShootingPower in level 2 ship
        spaceship2.addNormalShootingPower(-3.5f);
        assertEquals(2, spaceship2.getNormalShootingPower());
    }

    @Test
    void addFullDoubleCannonCount() {
        Spaceship spaceship = new Spaceship(1, 1);

        //adds one double cannon to DoubleCannonCount in level 1 ship
        spaceship.addFullDoubleCannonCount(1);
        assertEquals(1, spaceship.getFullDoubleCannonCount());

        //adds one double cannon to DoubleCannonCount in level 1 ship
        spaceship.addFullDoubleCannonCount(1);
        assertEquals(2, spaceship.getFullDoubleCannonCount());

        Spaceship spaceship2 = new Spaceship(2, 2);

        //adds one double cannon to DoubleCannonCount in level 2 ship
        spaceship2.addFullDoubleCannonCount(1);
        assertEquals(1, spaceship2.getFullDoubleCannonCount());

        //adds one double cannon to DoubleCannonCount in level 2 ship
        spaceship2.addFullDoubleCannonCount(1);
        assertEquals(2, spaceship2.getFullDoubleCannonCount());
    }

    @Test
    void addHalfDoubleCannonCount() {
        Spaceship spaceship = new Spaceship(1, 1);

        //adds one double cannon to DoubleCannonCount in level 1 ship
        spaceship.addHalfDoubleCannonCount(1);
        assertEquals(1, spaceship.getHalfDoubleCannonCount());

        //adds one double cannon to DoubleCannonCount in level 1 ship
        spaceship.addHalfDoubleCannonCount(1);
        assertEquals(2, spaceship.getHalfDoubleCannonCount());

        Spaceship spaceship2 = new Spaceship(2, 2);

        //adds one double cannon to DoubleCannonCount in level 2 ship
        spaceship2.addHalfDoubleCannonCount(1);
        assertEquals(1, spaceship2.getHalfDoubleCannonCount());

        //adds one double cannon to DoubleCannonCount in level 2 ship
        spaceship2.addHalfDoubleCannonCount(1);
        assertEquals(2, spaceship2.getHalfDoubleCannonCount());
    }

    @Test
    void addNormalEnginePower() {
        Spaceship spaceship = new Spaceship(1, 0);

        //adds two engine power to NormalEnginePower in level 1 ship
        spaceship.addNormalEnginePower(2);
        assertEquals(2, spaceship.getNormalEnginePower());

        //adds one point five engine power to NormalEnginePower in level 1 ship
        spaceship.addNormalEnginePower(2);
        assertEquals(4, spaceship.getNormalEnginePower());

        //removes two engine power to NormalEnginePower in level 1 ship
        spaceship.addNormalEnginePower(-2);
        assertEquals(2, spaceship.getNormalEnginePower());

        Spaceship spaceship2 = new Spaceship(2, 3);

        //adds two engine power to NormalEnginePower in level 2 ship
        spaceship2.addNormalEnginePower(2);
        assertEquals(2, spaceship2.getNormalEnginePower());

        //adds three point five engine power to NormalEnginePower in level 2 ship
        spaceship2.addNormalEnginePower(3);
        assertEquals(5, spaceship2.getNormalEnginePower());

        //removes three point five engine power to NormalEnginePower in level 2 ship
        spaceship2.addNormalEnginePower(-3);
        assertEquals(2, spaceship2.getNormalEnginePower());
    }

    @Test
    void addDoubleEngineCount() {
        Spaceship spaceship = new Spaceship(1, 1);

        //adds one double engine to DoubleEngineCount in level 1 ship
        spaceship.addDoubleEngineCount(1);
        assertEquals(1, spaceship.getDoubleEngineCount());

        //adds one double engine to DoubleEngineCount in level 1 ship
        spaceship.addDoubleEngineCount(1);
        assertEquals(2, spaceship.getDoubleEngineCount());

        Spaceship spaceship2 = new Spaceship(2, 0);

        //adds one double engine to DoubleEngineCount in level 2 ship
        spaceship2.addDoubleEngineCount(1);
        assertEquals(1, spaceship2.getDoubleEngineCount());

        //adds one double engine to DoubleEngineCount in level 2 ship
        spaceship2.addDoubleEngineCount(1);
        assertEquals(2, spaceship2.getDoubleEngineCount());
    }

    @Test
    void addLeftUpShieldCount() {
        Spaceship spaceship = new Spaceship(1, 1);

        //adds one shield to IdxShieldCount[i, 0, 0, i] in level 1 ship
        spaceship.addLeftUpShieldCount(1);
        assertEquals(1, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(1, spaceship.getIdxShieldCount(3));

        //adds one shield to IdxShieldCount[i, 0, 0, i] in level 1 ship
        spaceship.addLeftUpShieldCount(1);
        assertEquals(2, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(2, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addUpRightShieldCount() {
        Spaceship spaceship = new Spaceship(2, 2);

        //adds one shield to IdxShieldCount[i, i, 0, 0] in level 2 ship
        spaceship.addUpRightShieldCount(1);
        assertEquals(1, spaceship.getIdxShieldCount(0));
        assertEquals(1, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));

        //adds one shield to IdxShieldCount[i, i, 0, 0] in level 2 ship
        spaceship.addUpRightShieldCount(1);
        assertEquals(2, spaceship.getIdxShieldCount(0));
        assertEquals(2, spaceship.getIdxShieldCount(1));
        assertEquals(0, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addRightDownShieldCount() {
        Spaceship spaceship = new Spaceship(1, 3);

        //adds one shield to IdxShieldCount[0, i, i, 0] in level 1 ship
        spaceship.addRightDownShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(1, spaceship.getIdxShieldCount(1));
        assertEquals(1, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));

        //adds one shield to IdxShieldCount[0, i, i, 0] in level 1 ship
        spaceship.addRightDownShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(2, spaceship.getIdxShieldCount(1));
        assertEquals(2, spaceship.getIdxShieldCount(2));
        assertEquals(0, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addDownLeftShieldCount() {
        Spaceship spaceship = new Spaceship(2, 0);

        //adds one shield to IdxShieldCount[0, 0, i, i] in level 2 ship
        spaceship.addDownLeftShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(1, spaceship.getIdxShieldCount(2));
        assertEquals(1, spaceship.getIdxShieldCount(3));

        //adds one shield to IdxShieldCount[0, 0, i, i] in level 2 ship
        spaceship.addDownLeftShieldCount(1);
        assertEquals(0, spaceship.getIdxShieldCount(0));
        assertEquals(0, spaceship.getIdxShieldCount(1));
        assertEquals(2, spaceship.getIdxShieldCount(2));
        assertEquals(2, spaceship.getIdxShieldCount(3));
    }

    @Test
    void addBoxCount() {
        Spaceship spaceship = new Spaceship(1, 2);

        //adds one green box to BoxCount in level 1 ship
        spaceship.addBoxCount(1, Box.GREEN);
        assertArrayEquals(new int[] {0, 0, 1, 0}, spaceship.getBoxCounts());

        //adds one red box to BoxCount in level 1 ship
        spaceship.addBoxCount(1, Box.RED);
        assertArrayEquals(new int[] {1, 0, 1, 0}, spaceship.getBoxCounts());

        //adds one green box to BoxCount in level 1 ship
        spaceship.addBoxCount(1, Box.GREEN);
        assertArrayEquals(new int[] {1, 0, 2, 0}, spaceship.getBoxCounts());

        //adds one yellow box to BoxCount in level 1 ship
        spaceship.addBoxCount(1, Box.YELLOW);
        assertArrayEquals(new int[] {1, 1, 2, 0}, spaceship.getBoxCounts());

        //adds one blue box to BoxCount in level 1 ship
        spaceship.addBoxCount(1, Box.BLUE);
        assertArrayEquals(new int[] {1, 1, 2, 1}, spaceship.getBoxCounts());

        Spaceship spaceship2 = new Spaceship(2, 1);

        //adds one yellow box to BoxCount in level 2 ship
        spaceship2.addBoxCount(1, Box.YELLOW);
        assertArrayEquals(new int[] {0, 1, 0, 0}, spaceship2.getBoxCounts());

        //adds one yellow box to BoxCount in level 2 ship
        spaceship2.addBoxCount(1, Box.YELLOW);
        assertArrayEquals(new int[] {0, 2, 0, 0}, spaceship2.getBoxCounts());

        //adds one blue box to BoxCount in level 2 ship
        spaceship2.addBoxCount(1, Box.BLUE);
        assertArrayEquals(new int[] {0, 2, 0, 1}, spaceship2.getBoxCounts());

        //adds one green box to BoxCount in level 2 ship
        spaceship2.addBoxCount(1, Box.GREEN);
        assertArrayEquals(new int[] {0, 2, 1, 1}, spaceship2.getBoxCounts());
        
        //adds one red box to BoxCount in level 2 ship
        spaceship2.addBoxCount(1, Box.RED);
        assertArrayEquals(new int[] {1, 2, 1, 1}, spaceship2.getBoxCounts());
    }

    @Test
    void maxNumberOfDoubleEnginesUsable() {
        Player p1 = new Player("alice", 1, 2);

        p1.getSpaceship().addDoubleEngineCount(2);
        p1.getSpaceship().addBatteriesCount(3);

        assertEquals(2, p1.getSpaceship().maxNumberOfDoubleEnginesUsable());

        Player p2 = new Player("valeria", 1, 2);

        p2.getSpaceship().addDoubleEngineCount(2);
        p2.getSpaceship().addBatteriesCount(3);

        assertEquals(2, p2.getSpaceship().maxNumberOfDoubleEnginesUsable());

        Player p3 = new Player("anna", 1, 2);

        p3.getSpaceship().addDoubleEngineCount(3);
        p3.getSpaceship().addBatteriesCount(3);

        assertEquals(3, p3.getSpaceship().maxNumberOfDoubleEnginesUsable());

        Player p4 = new Player("matteo", 1, 2);

        p4.getSpaceship().addDoubleEngineCount(0);
        p4.getSpaceship().addBatteriesCount(3);

        assertEquals(0, p4.getSpaceship().maxNumberOfDoubleEnginesUsable());

        Player p5 = new Player("luca", 1, 2);

        p5.getSpaceship().addDoubleEngineCount(3);
        p5.getSpaceship().addBatteriesCount(0);

        assertEquals(0, p5.getSpaceship().maxNumberOfDoubleEnginesUsable());
    }

    @Test
    void maxNumberOfDoubleCannonsUsable() {
        Player p1 = new Player("alice", 1, 2);

        p1.getSpaceship().addFullDoubleCannonCount(1);
        p1.getSpaceship().addHalfDoubleCannonCount(1);
        p1.getSpaceship().addBatteriesCount(3);

        assertEquals(2, p1.getSpaceship().maxNumberOfDoubleCannonsUsable());

        Player p2 = new Player("valeria", 1, 2);

        p2.getSpaceship().addHalfDoubleCannonCount(2);
        p2.getSpaceship().addBatteriesCount(3);

        assertEquals(2, p2.getSpaceship().maxNumberOfDoubleCannonsUsable());

        Player p3 = new Player("anna", 1, 2);

        p3.getSpaceship().addFullDoubleCannonCount(3);
        p3.getSpaceship().addHalfDoubleCannonCount(3);
        p3.getSpaceship().addBatteriesCount(3);

        assertEquals(3, p3.getSpaceship().maxNumberOfDoubleCannonsUsable());

        Player p4 = new Player("matteo", 1, 2);

        p4.getSpaceship().addFullDoubleCannonCount(0);
        p4.getSpaceship().addHalfDoubleCannonCount(0);
        p4.getSpaceship().addBatteriesCount(3);

        assertEquals(0, p4.getSpaceship().maxNumberOfDoubleCannonsUsable());

        Player p5 = new Player("luca", 1, 2);

        p5.getSpaceship().addFullDoubleCannonCount(3);
        p5.getSpaceship().addHalfDoubleCannonCount(5);
        p5.getSpaceship().addBatteriesCount(0);

        assertEquals(0, p5.getSpaceship().maxNumberOfDoubleCannonsUsable());
    }

    @Test
    void checkShipAllowPurpleAlien(){
        Spaceship spaceship = new Spaceship(2, 0);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard.setHandComponent(housingUnit);
        buildingBoard.placeComponent(2, 2, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.initSpaceshipParams();
        assertTrue(spaceship.checkShipAllowPurpleAlien());

        Spaceship spaceship2 = new Spaceship(2, 0);
        BuildingBoard buildingBoard2 = spaceship2.getBuildingBoard();

        HousingUnit housingUnit2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard2.setHandComponent(housingUnit2);
        buildingBoard2.placeComponent(2, 2, 0);

        buildingBoard2.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(2, 1, 0);

        buildingBoard2.initSpaceshipParams();
        assertFalse(spaceship2.checkShipAllowPurpleAlien());
    }

    @Test
    void checkShipAllowOrangeAlien(){
        Spaceship spaceship = new Spaceship(2, 0);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard.setHandComponent(housingUnit);
        buildingBoard.placeComponent(2, 2, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.initSpaceshipParams();
        assertTrue(spaceship.checkShipAllowOrangeAlien());

        Spaceship spaceship2 = new Spaceship(2, 0);
        BuildingBoard buildingBoard2 = spaceship2.getBuildingBoard();

        HousingUnit housingUnit2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard2.setHandComponent(housingUnit2);
        buildingBoard2.placeComponent(2, 2, 0);

        buildingBoard2.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(2, 1, 0);

        buildingBoard2.initSpaceshipParams();
        assertFalse(spaceship2.checkShipAllowOrangeAlien());
    }
}