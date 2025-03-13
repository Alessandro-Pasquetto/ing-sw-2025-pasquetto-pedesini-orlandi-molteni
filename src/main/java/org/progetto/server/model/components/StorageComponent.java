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
    // OTHER METHODS
    // =======================

    // Adds the number of items defined as parameter to the component
    public boolean incrementItemsCount(int num) {
        if (num + itemsCount < capacity) {
            this.itemsCount += num;
            return true;
        } else {
            return false;
        }
    }

    // Removes the number of items defined as parameter from the component
    public boolean decrementItemsCount(int num) {
        if (num >= itemsCount) {
            this.itemsCount -= num;
            return true;
        } else {
            return false;
        }
    }
}
