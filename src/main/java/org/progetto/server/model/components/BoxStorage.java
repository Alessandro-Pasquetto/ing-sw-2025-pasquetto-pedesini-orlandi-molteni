package org.progetto.server.model.components;

public class BoxStorage extends Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private Box[] boxStorage;
    private boolean isRed;
    private int boxCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxStorage(ComponentType type, int[] connections, String imgSrc, int capacity, boolean isRed) {
        super(type, connections, imgSrc);
        this.boxStorage = new Box[capacity];
        this.isRed = isRed;
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

    public boolean isRed() {
        return isRed;
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
    public boolean addBox(Box box, int idx) {
        if (box != null && idx >= 0 && idx < boxStorage.length && boxStorage[idx] == null) {
            if (!box.getType().equals(BoxType.RED) || this.isRed) {  // Checks in case of red box if its is possible
                boxStorage[idx] = box;
                this.boxCount += 1;
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
    public boolean removeBox(int idx) {
        if (boxStorage[idx] != null) {
            boxStorage[idx] = null;
            this.boxCount -= 1;
            System.gc();
            return true;
        } else {
            return false;
        }
    }
}
