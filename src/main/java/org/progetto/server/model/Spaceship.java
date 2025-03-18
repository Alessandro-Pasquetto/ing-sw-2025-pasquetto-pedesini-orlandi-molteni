package org.progetto.server.model;

import org.progetto.server.model.components.BoxType;

public class Spaceship {
  
    // =======================
    // ATTRIBUTES
    // =======================

    private int componentShipCount;
    private int destroyedCount;
    private int crewCount;
    private int batteriesCount;
    private int exposedConnectorsCount;
    private boolean alienPurple;
    private boolean alienOrange;
    private float normalShootingPower;
    private int doubleCannonCount;
    private float normalEnginePower;
    private int doubleEngineCount;
    private final int[] shieldCounts;  // {up, right, down, left}
    private final int[] boxCounts;     // {red, yellow, green, blue}
    private final BuildingBoard buildingBoard;

    // =======================
    // CONSTRUCTORS
    // =======================

    // Initialize spaceship with initial values of attributes
    public Spaceship(int levelShip, int color) {
        componentShipCount = 1;
        destroyedCount = 0;
        crewCount = 0;
        batteriesCount = 0;
        exposedConnectorsCount = 0;
        alienPurple = false;
        alienOrange = false;
        normalShootingPower = 0;
        doubleCannonCount = 0;
        doubleEngineCount = 0;
        normalEnginePower = 0;
        shieldCounts = new int[] { 0, 0, 0, 0};
        boxCounts = new int[] {0, 0, 0, 0};
        buildingBoard = new BuildingBoard(levelShip, color,this);
    }

    // =======================
    // GETTERS
    // =======================

    public int getComponentShipCount() {
        return componentShipCount;
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

    public float getNormalShootingPower(){
        return normalShootingPower;
    }

    public int getDoubleCannonCount() {
        return doubleCannonCount;
    }

    public int getDoubleEngineCount(){
        return doubleEngineCount;
    }

    public float getNormalEnginePower(){
        return normalEnginePower;
    }

    public int getIdxShieldCount(int index){
        return shieldCounts[index];
    }

    public int[] getBoxCounts() {
        return boxCounts;
    }

    public BuildingBoard getBuildingBoard() {
        return buildingBoard;
    }

    // =======================
    // SETTERS
    // =======================

    public void setExposedConnectorsCount(int count) {
        exposedConnectorsCount = count;
    }

    public void setAlienPurple(boolean alienPresence) {
        alienPurple = alienPresence;
    }

    public void setAlienOrange(boolean alienPresence) {
        alienOrange = alienPresence;
    }

    // =======================
    // ADDERS
    // =======================

    public void addComponentShipCount(int countToAdd) {
        componentShipCount += countToAdd;
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

    public void addDoubleCannonCount(int countToAdd) {
        doubleCannonCount += countToAdd;
    }

    public void addNormalEnginePower(float powerToAdd) {
        normalEnginePower += powerToAdd;
    }

    public void addDoubleEngineCount(int countToAdd) {
        doubleEngineCount += countToAdd;
    }

    public void addLeftUpShieldCount(int countToAdd) {
        shieldCounts[3] += countToAdd;
        shieldCounts[0] += countToAdd;
    }

    public void addUpRightShieldCount(int countToAdd) {
        shieldCounts[0] += countToAdd;
        shieldCounts[1] += countToAdd;
    }

    public void addRightDownShieldCount(int countToAdd) {
        shieldCounts[1] += countToAdd;
        shieldCounts[2] += countToAdd;
    }

    public void addDownLeftShieldCount(int countToAdd) {
        shieldCounts[2] += countToAdd;
        shieldCounts[3] += countToAdd;
    }

    public void addBoxCount(int countToAdd, BoxType type) {
        switch (type)
        {
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
}

