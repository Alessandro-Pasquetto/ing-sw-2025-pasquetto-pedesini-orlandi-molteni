package org.progetto.server.model;

import java.io.Serializable;

public class Player implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String name;
    private int credits;
    private int color;
    private int position;
    private Spaceship spaceship;
    private boolean hasLeft;
    private boolean isReady;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Player(String name) {
        this.name = name;
        this.credits = 0;
        this.position = 0;
        this.hasLeft = false;
        this.isReady = false;
    }

    // =======================
    // GETTERS
    // =======================

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }

    public int getColor() {
        return color;
    }

    public int getPosition() {
        return position;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public boolean getHasLeft() {
        return hasLeft;
    }

    public boolean getIsReady() {
        return isReady;
    }

    // =======================
    // SETTERS
    // =======================

    public void setColor(int color) {
        this.color = color;
    }

    public void setPosition(int newPosition) {
        this.position = newPosition;
    }

    public void setHasLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    public void setIsReady(boolean isReady) {
        if(this.isReady != isReady)
            this.isReady = isReady;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Initialize the spaceship for a player
     *
     * @author Lorenzo
     * @param levelShip is the game level
     */
    public void initSpaceship(int levelShip) {
        this.spaceship = new Spaceship(levelShip, color);
    }

    /**
     * Adds credits to the player
     *
     * @author Alessandro
     * @param creditsToAdd is the number of credits gained (if positive) or lost (if negative)
     */
    public void addCredits(int creditsToAdd) {
        this.credits += creditsToAdd;
    }

    /**
     * Returns the sum of the roll of two dice
     *
     * @author Alessandro
     * @return the sum of the roll of two dice
     */
    public int rollDice() {
        int dice1 = (int) (Math.random() * 6) + 1;
        int dice2 = (int) (Math.random() * 6) + 1;
        return dice1 + dice2;
    }
}