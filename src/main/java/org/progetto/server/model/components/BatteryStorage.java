package org.progetto.server.model.components;

public class BatteryStorage extends Component {
    
    // =======================
    // ATTRIBUTES
    // =======================

    private int capacity;
    private int itemsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BatteryStorage(ComponentType type, int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.capacity = capacity;
        this.itemsCount = 0;
    }

    // =======================
    // GETTERS
    // =======================

    public int getCapacity() {
        return capacity;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Adds the number of items defined as parameter to the component
     *
     * @author Gabriele
     * @param num Amount to add
     * @return true if it got incremented, otherwise false
     */
    public boolean incrementItemsCount(int num) {
        if (num + itemsCount <= capacity) {
            this.itemsCount += num;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the number of items defined as parameter from the component
     *
     * @param num Amount to remove
     * @return true if it got decreased, otherwise false
     */
    public boolean decrementItemsCount(int num) {
        if (num <= itemsCount) {
            this.itemsCount -= num;
            return true;
        } else {
            return false;
        }
    }
}
