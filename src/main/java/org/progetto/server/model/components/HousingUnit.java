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
    private boolean allowAlienOrange;
    private boolean allowAlienPurple;

    // =======================
    // CONSTRUCTOR
    // =======================

    public HousingUnit(ComponentType type,int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.capacity = capacity;
        this.crewCount = 0;
        this.hasAlienOrange = false;
        this.hasAlienPurple = false;
        this.allowAlienOrange = false;
        this.allowAlienPurple = false;
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

    public boolean hasOrangeAlien() {
        return hasAlienOrange;
    }

    public boolean hasPurpleAlien() {
        return hasAlienPurple;
    }

    public boolean getAllowAlienOrange() {
        return allowAlienOrange;
    }

    public boolean getAllowAlienPurple() {
        return allowAlienPurple;
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

    public void setAllowAlienOrange(boolean allowAlienOrange) {
        this.allowAlienOrange = allowAlienOrange;
    }

    public void setAllowAlienPurple(boolean allowAlienPurple) {
        this.allowAlienPurple = allowAlienPurple;
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
     * @return true if the crew can be added
     */
    public boolean incrementCrewCount(Spaceship spaceship, int num) {

        if(crewCount + num > capacity)
            return false;

        spaceship.addCrewCount(num);
        crewCount += num;
        return true;
    }

    /**
     * Decrement the number of crew inside the unit
     *
     * @author Lorenzo
     * @param spaceship spaceship where we want to update parameters
     * @param num is the number of crew member to remove
     * @return true if the crew has been removed
     */
    public boolean decrementCrewCount(Spaceship spaceship, int num) {

        if(crewCount - num < 0)
            return false;

        spaceship.addCrewCount(-num);
        crewCount -= num;
        return true;
    }
}
