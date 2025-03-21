package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoxStorageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getBoxStorage() {
    }

    @Test
    void isRed() {
    }

    @Test
    void addBox() {
        BoxStorage bs1;
        BoxStorage bs2;

        // Adds a new box into a normal box storage
        bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3, false);
        Box box1 = new Box(BoxType.GREEN, 2);

        assertTrue(bs1.addBox(box1, 0));
        assertEquals(box1, bs1.getBoxStorage()[0]);

        // Tries to add a new box into a normal box storage in already taken place
        Box box2 = new Box(BoxType.GREEN, 2);

        assertFalse(bs1.addBox(box2, 0));
        assertEquals(box1, bs1.getBoxStorage()[0]);

        // Tries to add a new box into a normal box storage outside its indexes
        Box box3 = new Box(BoxType.GREEN, 2);

        assertFalse(bs1.addBox(box2, 4));

        // Tries to add a new RED box into a normal box storage
        Box box4 = new Box(BoxType.RED, 4);

        assertFalse(bs1.addBox(box4, 1));
        assertNull(bs1.getBoxStorage()[1]);


        // Adds a new box into a red box storage
        bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3, true);
        Box box5 = new Box(BoxType.GREEN, 2);

        assertTrue(bs2.addBox(box5, 0));
        assertEquals(box5, bs2.getBoxStorage()[0]);

        // Tries to add a new box into a red box storage in already taken place
        Box box6 = new Box(BoxType.GREEN, 2);

        assertFalse(bs2.addBox(box6, 0));
        assertEquals(box5, bs2.getBoxStorage()[0]);

        // Tries to add a new box into a red box storage outside its indexes
        Box box7 = new Box(BoxType.GREEN, 2);

        assertFalse(bs2.addBox(box7, 4));

        // Tries to add a new RED box into a red box storage
        Box box8 = new Box(BoxType.RED, 4);

        assertTrue(bs2.addBox(box8, 1));
        assertEquals(box8, bs2.getBoxStorage()[1]);
    }

    @Test
    void removeBox() {
        BoxStorage bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3, false);

        Box box = new Box(BoxType.GREEN, 2);
        bs.addBox(box, 0);

        // Tries to remove a present box
        assertTrue(bs.removeBox(0));
        assertNull(bs.getBoxStorage()[0]);

        // Tries to remove an empty cell
        assertFalse(bs.removeBox(1));
    }
}