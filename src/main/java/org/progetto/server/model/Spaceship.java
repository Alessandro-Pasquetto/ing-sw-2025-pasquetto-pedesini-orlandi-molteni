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
    private float shootingPower;
    private float enginePower;
    private int[] shields;
    private BuildingBoard buildingBoard;

    // =======================
    // CONSTRUCTORS
    // =======================
  
    // Initialize spaceship with initial values of attributes
    public Spaceship(int destroyed,int crewCount,int boxValue,int batteriesCount,int exposedConnectorsCount, boolean alienPurple,boolean alienOrange,float shootingPower, float enginePower,int[] shields) {
        this.destroyed = destroyed;
        this.crewCount = crewCount;
        this.boxValue = boxValue;
        this.batteriesCount = batteriesCount;
        this.exposedConnectorsCount = exposedConnectorsCount;
        this.alienPurple = alienPurple;
        this.alienOrange = alienOrange;
        this.shootingPower = shootingPower;
        this.enginePower = enginePower;
        this.shields = shields;
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
        return shootingPower;
    }

    public float getEnginePower(){
        return enginePower;
    }

    public int[] getShields() {
        return shields;
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
        shootingPower = power;
    }

    public void setEnginePower(float power) {
        enginePower = power;
    }

    public void setShields(int[] shields) {
        this.shields = shields;
    }

    // =======================
    // OTHER METHODS
    // =======================


}

