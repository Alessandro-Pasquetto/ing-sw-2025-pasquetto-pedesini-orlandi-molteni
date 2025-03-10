package org.progetto.server.model.components;

public class StorageComponent extends Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private int capacity;
    private int itemsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StorageComponent(ComponentType type, int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.capacity = capacity;
        this.itemsCount = 0;
    }

    // =======================
    // GETTERS
    // =======================

    public int getItemsCount() {
        return itemsCount;
    }

    public int getCapacity() {
        return capacity;
    }

    // =======================
    // SETTERS
    // =======================

    public void incrementItemsCount(int num) {
        this.itemsCount += num;
    }

    public void decrementItemsCount(int num) {
        this.itemsCount -= num;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Adds the number of items defined as parameter to the component
    public boolean addItem(int num) {
        if (num + itemsCount < capacity) {
            incrementItemsCount(num);
            return true;
        } else {
            return false;
        }
    }

    // Removes the number of items defined as parameter from the component
    public boolean removeItem(int num) {
        if (num >= itemsCount) {
            decrementItemsCount(num);
            return true;
        } else {
            return false;
        }
    }
}
