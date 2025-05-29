package org.progetto.server.model.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

        assertNotNull(bs.getBoxes());
        assertEquals(3, bs.getBoxes().length);
        assertNull(bs.getBoxes()[0]);
    }

    @Test
    void addBox() {
        Spaceship s = new Spaceship(1, 0);
        BoxStorage bs1;
        BoxStorage bs2;

        // Adds a new box into a normal box storage
        bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);
        Box box1 = Box.GREEN;

        bs1.addBox(s, box1, 0);
        assertEquals(box1, bs1.getBoxes()[0]);

        // Tries to add a new box into a normal box storage in already taken place
        Box box2 = Box.GREEN;

        assertThrows(IllegalStateException.class, () -> bs1.addBox(s, box2, 0));
        assertEquals(box1, bs1.getBoxes()[0]);

        // Tries to add a new box into a normal box storage outside its indexes
        Box box3 = Box.GREEN;

        assertThrows(IllegalStateException.class, () -> bs1.addBox(s, box2, 4));

        // Tries to add a new RED box into a normal box storage
        Box box4 = Box.RED;

        assertThrows(IllegalStateException.class, () -> bs1.addBox(s, box4, 1));
        assertNull(bs1.getBoxes()[1]);


        // Adds a new box into a red box storage
        bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);
        Box box5 = Box.GREEN;

        bs2.addBox(s, box5, 0);
        assertEquals(box5, bs2.getBoxes()[0]);

        // Tries to add a new box into a red box storage in already taken place
        Box box6 = Box.GREEN;

        assertThrows(IllegalStateException.class, () -> bs2.addBox(s, box6, 0));
        assertEquals(box5, bs2.getBoxes()[0]);

        // Tries to add a new box into a red box storage outside its indexes
        Box box7 = Box.GREEN;

        assertThrows(IllegalStateException.class, () -> bs2.addBox(s, box7, 4));
        // Tries to add a new RED box into a red box storage
        Box box8 = Box.RED;

        bs2.addBox(s, box8, 1);
        assertEquals(box8, bs2.getBoxes()[1]);
    }

    @Test
    void removeBox() {
        Spaceship s = new Spaceship(1, 0);

        BoxStorage bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);

        Box box = Box.GREEN;
        bs.addBox(s, box, 0);

        // Tries to remove a present box
        bs.removeBox(s, 0);
        assertNull(bs.getBoxes()[0]);

        // Tries to remove an empty cell
        assertThrows(IllegalStateException.class, () -> bs.removeBox(s, 1));
    }

    @Test
    void getBoxStorageValues(){
        Spaceship s = new Spaceship(1, 0);

        // Adds a new box into a normal box storage
        BoxStorage bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);

        Box box1 = Box.GREEN;
        assertEquals(2, box1.getValue());

        Box box2 = Box.YELLOW;
        Box box3 = Box.RED;

        bs1.addBox(s, box1, 2);
        bs1.addBox(s, box2, 0);
        assertThrows(IllegalStateException.class, () -> bs1.addBox(s, box3, 1));

        Box[] boxStorage = new Box[]{Box.YELLOW, null, Box.GREEN};

        assertArrayEquals(boxStorage, bs1.getBoxes());

        // Adds a new box into a normal box storage
        BoxStorage bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgSrc", 3);

        Box box4 = Box.BLUE;

        bs2.addBox(s, box1, 2);
        bs2.addBox(s, box4, 0);
        bs2.addBox(s, box3, 1);

        Box[]boxStorage2 = new Box[]{Box.BLUE, Box.RED, Box.GREEN};

        assertArrayEquals(boxStorage2, bs2.getBoxes());
    }
}