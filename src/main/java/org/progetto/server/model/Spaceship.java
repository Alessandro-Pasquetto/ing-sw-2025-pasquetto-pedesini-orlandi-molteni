package org.progetto.server.model;

import org.progetto.server.model.components.*;

import java.io.Serializable;

public class Spaceship implements Serializable {
  
    // =======================
    // ATTRIBUTES
    // =======================

    private final int levelShip;
    private int shipComponentsCount;
    private int destroyedCount;
    private int crewCount;
    private int batteriesCount;
    private int exposedConnectorsCount;
    private boolean alienPurple;
    private boolean alienOrange;
    private boolean increasedShootingPowerByAlien;
    private boolean increasedEnginePowerByAlien;
    private float normalShootingPower;
    private int halfDoubleCannonCount;  // double cannons with half shooting power
    private int fullDoubleCannonCount;  // double cannons with full shooting power
    private int normalEnginePower;
    private int doubleEngineCount;
    private final int[] shieldCounts;  // {up, right, down, left}
    private final int[] boxCounts;     // {red, yellow, green, blue}
    private final BuildingBoard buildingBoard;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Spaceship(int levelShip, int color) {
        this.levelShip = levelShip;
        this.shipComponentsCount = 1;
        this.destroyedCount = 0;
        this.crewCount = 0;
        this.batteriesCount = 0;
        this.exposedConnectorsCount = 0;
        this.alienPurple = false;
        this.alienOrange = false;
        this.normalShootingPower = 0;
        this.halfDoubleCannonCount = 0;
        this.fullDoubleCannonCount = 0;
        this.doubleEngineCount = 0;
        this.normalEnginePower = 0;
        this.shieldCounts = new int[] {0, 0, 0, 0};
        this.boxCounts = new int[] {0, 0, 0, 0};
        this.buildingBoard = new BuildingBoard(this, color);
    }

    // =======================
    // GETTERS
    // =======================

    public int getLevelShip() {return levelShip;}

    public int getShipComponentsCount() {
        return shipComponentsCount;
    }

    public int getDestroyedCount() {
        return destroyedCount;
    }

    public int getCrewCount() {
        return crewCount;
    }

    public int getBoxesValue() {
        return 4 * boxCounts[0] + 3 * boxCounts[1] + 2 * boxCounts[2] + boxCounts[3];
    }

    public int getBatteriesCount() {
        return batteriesCount;
    }

    public int getExposedConnectorsCount() {
        return exposedConnectorsCount;
    }

    public boolean getAlienPurple() {
        return alienPurple;
    }

    public boolean getAlienOrange() {
        return alienOrange;
    }

    public boolean getIncreasedShootingPowerByAlien() {
        return increasedShootingPowerByAlien;
    }

    public boolean getIncreasedEnginePowerByAlien() {
        return increasedEnginePowerByAlien;
    }

    public float getNormalShootingPower(){
        return normalShootingPower;
    }

    public int getHalfDoubleCannonCount() {
        return halfDoubleCannonCount;
    }

    public int getFullDoubleCannonCount() {
        return fullDoubleCannonCount;
    }

    public int getDoubleEngineCount(){
        return doubleEngineCount;
    }

    public int getNormalEnginePower(){
        return normalEnginePower;
    }

    public int getIdxShieldCount(int index){
        return shieldCounts[index];
    }

    public int[] getBoxCounts() {
        return boxCounts;
    }

    public int getBoxesCount(){
        return boxCounts[0] +  boxCounts[1] + boxCounts[2] + boxCounts[3];
    }

    public BuildingBoard getBuildingBoard() {
        return buildingBoard;
    }

    public int getTotalCrewCount() {
        return crewCount;
    }

    // =======================
    // SETTERS
    // =======================

    public void setExposedConnectorsCount(int count) {
        exposedConnectorsCount = count;
    }

    public void setAlienPurple(boolean alienPresence) throws IllegalStateException {
        if(alienPurple && alienPresence)
            throw new IllegalStateException("HasAlreadyPurpleAlien");
        alienPurple = alienPresence;
    }

    public void setAlienOrange(boolean alienPresence) {
        if(alienOrange && alienPresence)
            throw new IllegalStateException("HasAlreadyOrangeAlien");
        alienOrange = alienPresence;
    }

    public void setIncreasedShootingPowerByAlien(boolean increasedShootingPowerByAlien) {
        this.increasedShootingPowerByAlien = increasedShootingPowerByAlien;
    }

    public void setIncreasedEnginePowerByAlien(boolean increasedEnginePowerByAlien) {
        this.increasedEnginePowerByAlien = increasedEnginePowerByAlien;
    }

    // =======================
    // ADDERS
    // =======================

    public void addComponentsShipCount(int countToAdd) {
        shipComponentsCount += countToAdd;
    }

    public void addDestroyedCount(int countToAdd) {
        destroyedCount += countToAdd;
    }

    public void addCrewCount(int countToAdd) {
        crewCount += countToAdd;
    }

    public void addBatteriesCount(int countToAdd) {
        batteriesCount += countToAdd;
    }

    public void addNormalShootingPower(float powerToAdd) {
        normalShootingPower += powerToAdd;
    }

    public void addHalfDoubleCannonCount(int countToAdd) {
        halfDoubleCannonCount += countToAdd;
    }

    public void addFullDoubleCannonCount(int countToAdd) {fullDoubleCannonCount += countToAdd;}

    public void addNormalEnginePower(int powerToAdd) {
        normalEnginePower += powerToAdd;
    }

    public void addDoubleEngineCount(int countToAdd) {
        doubleEngineCount += countToAdd;
    }

    /**
     * Increment the shield counter towards the left-up
     *
     * @author Alessandro
     * @param countToAdd the number of shields to add
     */
    public void addLeftUpShieldCount(int countToAdd) {
        shieldCounts[3] += countToAdd;
        shieldCounts[0] += countToAdd;
    }

    /**
     * Increment the shield counter towards the up-right
     *
     * @author Alessandro
     * @param countToAdd the number of shields to add
     */
    public void addUpRightShieldCount(int countToAdd) {
        shieldCounts[0] += countToAdd;
        shieldCounts[1] += countToAdd;
    }

    /**
     * Increment the shield counter towards the right-down
     *
     * @author Alessandro
     * @param countToAdd the number of shields to add
     */
    public void addRightDownShieldCount(int countToAdd) {
        shieldCounts[1] += countToAdd;
        shieldCounts[2] += countToAdd;
    }

    /**
     * Increment the shield counter towards the down-left
     *
     * @author Alessandro
     * @param countToAdd the number of shields to add
     */
    public void addDownLeftShieldCount(int countToAdd) {
        shieldCounts[2] += countToAdd;
        shieldCounts[3] += countToAdd;
    }

    /**
     * Increase the number of boxes of the specified type
     *
     * @author Lorenzo
     * @param countToAdd the number of boxes to add
     * @param box the box to add
     */
    public void addBoxCount(int countToAdd, Box box) {
        switch (box) {
            case RED:
                boxCounts[0] += countToAdd;
                break;

            case YELLOW:
                boxCounts[1] += countToAdd;
                break;

            case GREEN:
                boxCounts[2] += countToAdd;
                break;

            case BLUE:
                boxCounts[3] += countToAdd;
                break;
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Reset the counter of destroyed components
     *
     * @author Alessandro
     */
    public void resetDestroyedCount() {
        destroyedCount = 0;
    }

    /**
     * Calculates maximum number of double engines usable
     *
     * @author Gabriele
     * @return maximum number of double engines usable
     */
    public int maxNumberOfDoubleEnginesUsable() {
        return Math.min(doubleEngineCount, batteriesCount);
    }

    /**
     * Calculates maximum number of double cannons usable
     *
     * @author Gabriele
     * @return maximum number of double cannons usable
     */
    public int maxNumberOfDoubleCannonsUsable() {
        int doubleCannonCount = halfDoubleCannonCount + fullDoubleCannonCount;
        return Math.min(doubleCannonCount, batteriesCount);
    }

    /**
     * Checks if there is at least a housing unit that can host a purple alien
     *
     * @author Alessandro
     * @return true if the spaceship allows purple alien
     */
    public boolean checkShipAllowPurpleAlien() {

        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrixCopy();

        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for (int x = 0; x < spaceshipMatrix[y].length; x++) {

                Component component = spaceshipMatrix[y][x];

                if(component == null)
                    continue;

                if(component instanceof HousingUnit hu && hu.getAllowPurpleAlien())
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is at least a housing unit that can host an orange alien
     *
     * @author Alessandro
     * @return true if the spaceship allows orange alien
     */
    public boolean checkShipAllowOrangeAlien() {

        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrixCopy();

        for (int y = 0; y < spaceshipMatrix.length; y++) {
            for (int x = 0; x < spaceshipMatrix[y].length; x++) {

                Component component = spaceshipMatrix[y][x];

                if(component == null)
                    continue;

                if(component instanceof HousingUnit hu && hu.getAllowOrangeAlien())
                    return true;
            }
        }
        return false;
    }
}