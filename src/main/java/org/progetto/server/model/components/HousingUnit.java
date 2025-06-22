package org.progetto.server.model.components;

import org.progetto.server.model.Spaceship;

public class HousingUnit extends Component{

    // =======================
    // ATTRIBUTES
    // =======================

    private int capacity;
    private int crewCount;
    private boolean hasAlienOrange;
    private boolean hasAlienPurple;
    private boolean allowOrangeAlien;
    private boolean allowPurpleAlien;

    // =======================
    // CONSTRUCTOR
    // =======================

    public HousingUnit(ComponentType type, int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.capacity = capacity;
        this.crewCount = 0;
        this.hasAlienOrange = false;
        this.hasAlienPurple = false;
        this.allowOrangeAlien = false;
        this.allowPurpleAlien = false;
    }

    // =======================
    // GETTERS
    // =======================

    public int getCapacity() {
        return capacity;
    }

    public int getCrewCount() {
        return crewCount;
    }

    public boolean getHasOrangeAlien() {
        return hasAlienOrange;
    }

    public boolean getHasPurpleAlien() {
        return hasAlienPurple;
    }

    public boolean getAllowOrangeAlien() {
        return allowOrangeAlien;
    }

    public boolean getAllowPurpleAlien() {
        return allowPurpleAlien;
    }

    // =======================
    // SETTERS
    // =======================

    public void setCrewCount(int crewCount) {
        this.crewCount = crewCount;
    }

    public void setAlienOrange(boolean hasAlienOrange) {
        this.hasAlienOrange = hasAlienOrange;
    }

    public void setAlienPurple(boolean hasAlienPurple) {
        this.hasAlienPurple = hasAlienPurple;
    }

    public void setAllowOrangeAlien(boolean allowOrangeAlien) {
        this.allowOrangeAlien = allowOrangeAlien;
    }

    public void setAllowPurpleAlien(boolean allowPurpleAlien) {
        this.allowPurpleAlien = allowPurpleAlien;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Increment the number of crew inside the unit
     *
     * @author Lorenzo
     * @param spaceship spaceship where we want to update parameters
     * @param num is the number of crew member to add
     */
    public void incrementCrewCount(Spaceship spaceship, int num) throws IllegalStateException {
        if (crewCount + num > capacity)
            throw new IllegalStateException("CapacityExceeded");

        spaceship.addCrewCount(num);
        crewCount += num;
    }

    /**
     * Decrement the number of crew inside the unit
     *
     * @author Lorenzo
     * @param spaceship spaceship where we want to update parameters
     * @param num is the number of crew member to remove
     */
    public void decrementCrewCount(Spaceship spaceship, int num) throws IllegalStateException {
        if (crewCount - num < 0)
            throw new IllegalStateException("CannotDecrement");

        spaceship.addCrewCount(-num);
        crewCount -= num;
    }
}
