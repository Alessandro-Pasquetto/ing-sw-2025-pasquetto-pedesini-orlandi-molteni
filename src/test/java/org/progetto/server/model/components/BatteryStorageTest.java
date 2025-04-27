package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Spaceship;

import static org.junit.jupiter.api.Assertions.*;

class BatteryStorageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCapacity() {
        BatteryStorage bs = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 0, 1, 0}, "imgSrc", 4);

        assertEquals(4, bs.getCapacity());
    }

    @Test
    void getItemsCount() {
        Spaceship spaceship = new Spaceship(2, 1);
        BatteryStorage bs = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 0, 1, 0}, "imgSrc", 4);

        assertEquals(0, bs.getItemsCount());

        bs.incrementItemsCount(spaceship, 2);
        assertEquals(2, bs.getItemsCount());
    }

    @Test
    void incrementItemsCount() {
        BatteryStorage bs = new BatteryStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);

        Spaceship spaceship = new Spaceship(2, 1);

        // Adds batteries to the battery storage
        assertTrue(bs.incrementItemsCount(spaceship, 1));
        assertEquals(1, bs.getItemsCount());

        // Tries to add more batteries, but it is full
        assertFalse(bs.incrementItemsCount(spaceship, 2));
        assertEquals(1, bs.getItemsCount());
    }

    @Test
    void decrementItemsCount() {
        BatteryStorage bs = new BatteryStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);

        Spaceship spaceship = new Spaceship(2, 1);


        bs.incrementItemsCount(spaceship, 2);

        // Removes batteries from the battery storage
        assertTrue(bs.decrementItemsCount(spaceship, 1));
        assertEquals(1, bs.getItemsCount());

        // Tries to remove batteries, but there aren't enough
        assertFalse(bs.decrementItemsCount(spaceship, 2));
        assertEquals(1, bs.getItemsCount());
    }
}