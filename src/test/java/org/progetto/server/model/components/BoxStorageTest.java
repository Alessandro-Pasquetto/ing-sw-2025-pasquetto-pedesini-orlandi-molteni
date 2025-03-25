package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;

import static org.junit.jupiter.api.Assertions.*;

class BoxStorageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCapacity() {
        // Setup
        BoxStorage bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);

        assertEquals(3, bs.getCapacity());
    }

    @Test
    void getBoxStorage() {
        BoxStorage bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);

        assertNotNull(bs.getBoxStorage());
        assertEquals(3, bs.getBoxStorage().length);
        assertNull(bs.getBoxStorage()[0]);
    }

    @Test
    void addBox() {
        Spaceship s = new Spaceship(1, 0);
        BoxStorage bs1;
        BoxStorage bs2;

        // Adds a new box into a normal box storage
        bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);
        Box box1 = Box.GREEN;

        assertTrue(bs1.addBox(s, box1, 0));
        assertEquals(box1, bs1.getBoxStorage()[0]);

        // Tries to add a new box into a normal box storage in already taken place
        Box box2 = Box.GREEN;

        assertFalse(bs1.addBox(s, box2, 0));
        assertEquals(box1, bs1.getBoxStorage()[0]);

        // Tries to add a new box into a normal box storage outside its indexes
        Box box3 = Box.GREEN;

        assertFalse(bs1.addBox(s, box2, 4));

        // Tries to add a new RED box into a normal box storage
        Box box4 = Box.RED;

        assertFalse(bs1.addBox(s, box4, 1));
        assertNull(bs1.getBoxStorage()[1]);


        // Adds a new box into a red box storage
        bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);
        Box box5 = Box.GREEN;

        assertTrue(bs2.addBox(s, box5, 0));
        assertEquals(box5, bs2.getBoxStorage()[0]);

        // Tries to add a new box into a red box storage in already taken place
        Box box6 = Box.GREEN;

        assertFalse(bs2.addBox(s, box6, 0));
        assertEquals(box5, bs2.getBoxStorage()[0]);

        // Tries to add a new box into a red box storage outside its indexes
        Box box7 = Box.GREEN;

        assertFalse(bs2.addBox(s, box7, 4));

        // Tries to add a new RED box into a red box storage
        Box box8 = Box.RED;

        assertTrue(bs2.addBox(s, box8, 1));
        assertEquals(box8, bs2.getBoxStorage()[1]);
    }

    @Test
    void removeBox() {
        Spaceship s = new Spaceship(1, 0);

        BoxStorage bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);

        Box box = Box.GREEN;
        bs.addBox(s, box, 0);

        // Tries to remove a present box
        assertTrue(bs.removeBox(s, 0));
        assertNull(bs.getBoxStorage()[0]);

        // Tries to remove an empty cell
        assertFalse(bs.removeBox(s, 1));
    }
}