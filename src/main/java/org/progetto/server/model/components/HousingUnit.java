package org.progetto.server.model.components;

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

    public void setAlienOrange(boolean hasAlienOrange) {
        this.hasAlienOrange = this.hasAlienOrange;
    }

    public void setPurpleAlien(boolean hasAlienPurple) {
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
     * increment the number of crew inside the unit
     * @author Lorenzo
     * @param num is the number of crew member to add
     * @return true if the crew can be added
     */
    public boolean incrementCrewCount(int num) {
        if(crewCount + num <= capacity) {
            crewCount += num;
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * decrement the number of crew inside the unit
     * @author Lorenzo
     * @param num is the number of crew member to remove
     * @return true if the crew has been removed
     */
    public boolean decrementCrewCount(int num) {
        if(crewCount - num >= 0) {
            crewCount -= num;
            return true;
        }
        else{
            return false;
        }
    }


}
