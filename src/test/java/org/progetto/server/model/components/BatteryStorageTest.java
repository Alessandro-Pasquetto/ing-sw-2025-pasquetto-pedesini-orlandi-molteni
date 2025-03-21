package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatteryStorageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCapacity() {
    }

    @Test
    void getItemsCount() {
    }

    @Test
    void incrementItemsCount() {
        BatteryStorage bs = new BatteryStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);

        // Adds batteries to the battery storage
        assertTrue(bs.incrementItemsCount(1));
        assertEquals(1, bs.getItemsCount());

        // Tries to add more batteries, but it is full
        assertFalse(bs.incrementItemsCount(2));
        assertEquals(1, bs.getItemsCount());
    }

    @Test
    void decrementItemsCount() {
        BatteryStorage bs = new BatteryStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);

        bs.incrementItemsCount(2);

        // Removes batteries from the battery storage
        assertTrue(bs.decrementItemsCount(1));
        assertEquals(1, bs.getItemsCount());

        // Tries to remove batteries, but there aren't enough
        assertFalse(bs.decrementItemsCount(2));
        assertEquals(1, bs.getItemsCount());
    }
}