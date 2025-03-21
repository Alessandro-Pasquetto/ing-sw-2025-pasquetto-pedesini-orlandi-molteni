package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HousingUnitTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCapacity() {
    }

    @Test
    void getCrewCount() {
    }

    @Test
    void hasOrangeAlien() {
    }

    @Test
    void hasPurpleAlien() {
    }

    @Test
    void getAllowAlienOrange() {
    }

    @Test
    void getAllowAlienPurple() {
    }

    @Test
    void setAlienOrange() {
    }

    @Test
    void setAlienPurple() {
    }

    @Test
    void setAllowAlienOrange() {
    }

    @Test
    void setAllowAlienPurple() {
    }

    @Test
    void incrementCrewCount() {
        HousingUnit unit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);

        // Adds crew to the housing unit
        assertTrue(unit.incrementCrewCount(1));
        assertEquals(1, unit.getCrewCount());

        // Tries to add more crew, but it is full
        assertFalse(unit.incrementCrewCount(2));
        assertEquals(1, unit.getCrewCount());
    }

    @Test
    void decrementCrewCount() {
        HousingUnit unit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);

        unit.incrementCrewCount(2);

        // Removes batteries from the battery storage
        assertTrue(unit.decrementCrewCount(1));
        assertEquals(1, unit.getCrewCount());

        // Tries to remove batteries, but there aren't enough
        assertFalse(unit.decrementCrewCount(2));
        assertEquals(1, unit.getCrewCount());
    }
}