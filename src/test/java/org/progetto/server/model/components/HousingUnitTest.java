package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Spaceship;

import static org.junit.jupiter.api.Assertions.*;

class HousingUnitTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCapacity() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        assertEquals(0, housingUnit.getCrewCount());

        housingUnit.setCrewCount(2);
        assertEquals(2, housingUnit.getCrewCount());
    }

    @Test
    void getCrewCount() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        assertEquals(0, housingUnit.getCrewCount());

        housingUnit.setCrewCount(2);
        assertEquals(2, housingUnit.getCrewCount());
    }

    @Test
    void hasOrangeAlien() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        assertFalse(housingUnit.hasOrangeAlien());

        housingUnit.setAlienOrange(true);
        assertTrue(housingUnit.hasOrangeAlien());
    }

    @Test
    void hasPurpleAlien() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        assertFalse(housingUnit.hasPurpleAlien());

        housingUnit.setAlienPurple(true);
        assertTrue(housingUnit.hasPurpleAlien());
    }

    @Test
    void getAllowAlienOrange() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        assertFalse(housingUnit.getAllowAlienOrange());

        housingUnit.setAllowAlienOrange(true);
        assertTrue(housingUnit.getAllowAlienOrange());
    }

    @Test
    void getAllowAlienPurple() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        assertFalse(housingUnit.getAllowAlienPurple());

        housingUnit.setAllowAlienPurple(true);
        assertTrue(housingUnit.getAllowAlienPurple());
    }

    @Test
    void setCrewCount() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 5);

        housingUnit.setCrewCount(3);
        assertEquals(3, housingUnit.getCrewCount());
    }

    @Test
    void setAlienOrange() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 2);

        housingUnit.setAlienOrange(true);
        assertTrue(housingUnit.hasOrangeAlien());
        housingUnit.setAlienOrange(false);
        assertFalse(housingUnit.hasOrangeAlien());
    }

    @Test
    void setAlienPurple() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        housingUnit.setAlienPurple(true);
        assertTrue(housingUnit.hasPurpleAlien());
        housingUnit.setAlienPurple(false);
        assertFalse(housingUnit.hasPurpleAlien());
    }

    @Test
    void setAllowAlienOrange() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        housingUnit.setAllowAlienOrange(true);
        assertTrue(housingUnit.getAllowAlienOrange());
        housingUnit.setAllowAlienOrange(false);
        assertFalse(housingUnit.getAllowAlienOrange());
    }

    @Test
    void setAllowAlienPurple() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 0, 1, 0}, "imgSrc", 3);

        housingUnit.setAllowAlienPurple(true);
        assertTrue(housingUnit.getAllowAlienPurple());
        housingUnit.setAllowAlienPurple(false);
        assertFalse(housingUnit.getAllowAlienPurple());
    }

    @Test
    void incrementCrewCount() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);
        Spaceship spaceship = new Spaceship(2,1);
        // Adds crew to the housing unit
        assertTrue(housingUnit.incrementCrewCount(spaceship,1));
        assertEquals(1, housingUnit.getCrewCount());

        // Tries to add more crew, but it is full
        assertFalse(housingUnit.incrementCrewCount(spaceship,2));
        assertEquals(1, housingUnit.getCrewCount());
    }

    @Test
    void decrementCrewCount() {
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);
        Spaceship spaceship = new Spaceship(2,1);

        housingUnit.incrementCrewCount(spaceship, 2);

        // Removes batteries from the battery storage
        assertTrue(housingUnit.decrementCrewCount(spaceship,1));
        assertEquals(1, housingUnit.getCrewCount());

        // Tries to remove batteries, but there aren't enough
        assertFalse(housingUnit.decrementCrewCount(spaceship,2));
        assertEquals(1, housingUnit.getCrewCount());
    }
}