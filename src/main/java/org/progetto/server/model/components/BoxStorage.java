package org.progetto.server.model.components;

import org.progetto.server.model.Spaceship;

public class BoxStorage extends Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Box[] boxStorage;
    private int boxCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxStorage(ComponentType type, int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.boxStorage = new Box[capacity];
        this.boxCount = 0;
    }

    // =======================
    // GETTERS
    // =======================

    public int getCapacity() {
        return boxStorage.length;
    }

    public Box[] getBoxStorage() {
        return boxStorage;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Adds the given box to boxStorage at the given index idx
     *
     * @author Gabriele
     * @param box Box to add
     * @param idx Storage index where to add it
     * @return true if the box is added, otherwise false
     */
    public boolean addBox(Spaceship spaceship, Box box, int idx) {
        if (box != null && idx >= 0 && idx < boxStorage.length && boxStorage[idx] == null) {
            if (box != Box.RED || type.equals(ComponentType.RED_BOX_STORAGE)) {  // Checks in case of red box if its is possible
                boxStorage[idx] = box;
                this.boxCount += 1;
                spaceship.addBoxCount(1, box);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the box from boxStorage at the given index idx
     *
     * @author Gabriele
     * @param idx Storage index to remove
     * @return true if the box is deleted, otherwise false
     */
    public boolean removeBox(Spaceship spaceship, int idx) {
        if(idx >= 0 && idx < boxStorage.length) {
            if (boxStorage[idx] != null) {
                spaceship.addBoxCount(-1, boxStorage[idx]);
                boxStorage[idx] = null;
                this.boxCount -= 1;
                System.gc();
                return true;
            } else {
                return false;
            }
        }else{
            return false;
        }
    }
}
