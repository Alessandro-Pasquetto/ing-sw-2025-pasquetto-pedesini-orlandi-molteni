package org.progetto.server.model.components;

public class BoxStorageComponent extends StorageComponent {

    // =======================
    // ATTRIBUTES
    // =======================

    private Box[] boxStorage;
    private boolean isRed;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxStorageComponent(ComponentType type, int[] connections, String imgSrc, int capacity, boolean isRed) {
        super(type, connections, imgSrc, capacity);
        this.boxStorage = new Box[capacity];
        this.isRed = isRed;
    }

    // =======================
    // GETTERS
    // =======================

    public Box[] getBoxStorage() {
        return boxStorage;
    }

    public boolean isRed() {
        return isRed;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Adds the given box to boxStorage at the given index idx
    public boolean addBox(Box box, int idx) {
        if (box != null && idx >= 0 && idx < boxStorage.length && boxStorage[idx] == null) {
            boxStorage[idx] = box;
            this.incrementItemsCount(1);
            return true;
        } else {
            return false;
        }
    }

    // Remove the box from boxStorage at the given index idx
    public boolean removeBox(int idx) {
        if (boxStorage[idx] != null) {
            boxStorage[idx] = null;
            this.decrementItemsCount(1);
            return true;
        } else {
            return false;
        }
    }
}
