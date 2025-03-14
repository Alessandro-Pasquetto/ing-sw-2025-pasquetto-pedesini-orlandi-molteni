package org.progetto.server.model.components;

public class StorageComponent extends Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int capacity;
    private int itemsCount;           //does not include aliens
    private boolean hasOrangeAlien;   //identify the presence of an alien if it's a housing unit
    private boolean hasPurpleAlien;

    // =======================
    // CONSTRUCTORS
    // =======================

    public StorageComponent(ComponentType type, int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.capacity = capacity;
        this.itemsCount = 0;
        this.hasOrangeAlien = false;
        this.hasPurpleAlien = false;
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

    public boolean getOrangeAlien() {
        return hasOrangeAlien;
    }

    public boolean getPurpleAlien() {
        return hasPurpleAlien;
    }



    // =======================
    // SETTERS
    // =======================
    public void setOrangeAlien(boolean orangeAlien) {
        this.hasOrangeAlien = orangeAlien;
    }

    public void setPurpleAlien(boolean purpleAlien) {
        this.hasPurpleAlien = purpleAlien;
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
        if (num <= itemsCount) {
            this.itemsCount -= num;
            return true;
        } else {
            return false;
        }
    }
}
