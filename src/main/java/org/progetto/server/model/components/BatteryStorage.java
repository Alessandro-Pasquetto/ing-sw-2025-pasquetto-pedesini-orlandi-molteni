package org.progetto.server.model.components;

import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;

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
    public boolean incrementItemsCount(Spaceship spaceship,int num) {
        if (num + itemsCount <= capacity) {
            spaceship.addBatteriesCount(num);
            this.itemsCount += num;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the number of items defined as parameter from the component
     *
     * @author Gabriele
     * @param num Amount to remove
     * @return true if it got decreased, otherwise false
     */
    public boolean decrementItemsCount(Spaceship spaceship,int num) {
        if (num <= itemsCount) {
            spaceship.addBatteriesCount(-num);
            this.itemsCount -= num;
            return true;
        } else {
            return false;
        }
    }
}
