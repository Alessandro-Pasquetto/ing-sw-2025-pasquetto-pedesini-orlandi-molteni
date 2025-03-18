package org.progetto.server.model.components;

public class HousingUnit extends Component{

    // =======================
    // ATTRIBUTES
    // =======================

    private int capacity;
    private int crewCount;
    private boolean hasOrangeAlien;
    private boolean hasPurpleAlien;


    // =======================
    // CONSTRUCTOR
    // =======================

    public HousingUnit(ComponentType type,int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.capacity = capacity;
        this.crewCount = 0;
        this.hasOrangeAlien = false;
        this.hasPurpleAlien = false;
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
        return hasOrangeAlien;
    }

    public boolean hasPurpleAlien() {
        return hasPurpleAlien;
    }

    // =======================
    // SETTERS
    // =======================

    public void setOrangeAlien(boolean hasOrangeAlien) {
        this.hasOrangeAlien = hasOrangeAlien;
    }

    public void setPurpleAlien(boolean hasPurpleAlien) {
        this.hasPurpleAlien = hasPurpleAlien;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
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
