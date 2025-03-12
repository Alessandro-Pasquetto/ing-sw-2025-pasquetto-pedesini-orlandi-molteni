package org.progetto.server.model;

public class Spaceship {
  
    // =======================
    // ATTRIBUTES
    // =======================
  
    private int destroyed;
    private int crewCount;
    private int boxValue;
    private int batteriesCount;
    private int exposedConnectorsCount;
    private boolean alienPurple;
    private boolean alienOrange;
    private float normalShootingPower;
    private int doubleCannonCount;
    private float normalEnginePower;
    private int doubleEngineCount;
    private int[] shields;
    private BuildingBoard buildingBoard;

    // =======================
    // CONSTRUCTORS
    // =======================

    // Initialize spaceship with initial values of attributes
    public Spaceship(int levelShip) {
        destroyed = 0;
        crewCount = 0;
        boxValue = 0;
        batteriesCount = 0;
        exposedConnectorsCount = 0;
        alienPurple = false;
        alienOrange = false;
        normalShootingPower = 0;
        doubleCannonCount = 0;
        doubleEngineCount = 0;
        normalEnginePower = 0;
        shields = new int[] { 0, 0, 0, 0};
        buildingBoard = new BuildingBoard(levelShip);
    }

    // =======================
    // GETTERS
    // =======================

    public int getDestroyedCount() {
        return destroyed;
    }

    public int getCrewCount() {
        return crewCount;
    }

    public int getBoxValue() {
        return boxValue;
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

    public float getShootingPower(){
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

    public int[] getShields() {
        return shields;
    }

    public BuildingBoard getBuildingBoard() {
        return buildingBoard;
    }
 
    // =======================
    // SETTERS
    // =======================

    public void setDestroyedCount(int destroyed) {
        this.destroyed = destroyed;
    }

    public void setCrew(int quantity) {
        crewCount = quantity;
    }

    public void setBoxValue(int value) {
        boxValue = value;
    }

    public void setBatteriesCount(int quantity) {
        batteriesCount = quantity;
    }

    public void setExposedConnectorsCount(int count) {
        exposedConnectorsCount = count;
    }

    public void setAlienPurple(boolean alien) {
        alienPurple = alien;
    }

    public void setAlienOrange(boolean alien) {
        alienOrange = alien;
    }

    public void setShootingPower(float power) {
        normalShootingPower = power;
    }

    public void setDoubleCannonCount(int count) {
        doubleCannonCount = count;
    }

    public void setNormalEnginePower(float power) {
        normalEnginePower = power;
    }

    public void setDoubleEngineCount(int count) {
        doubleEngineCount = count;
    }

    public void setShields(int[] shields) {
        this.shields = shields;
    }

    // =======================
    // OTHER METHODS
    // =======================


}

