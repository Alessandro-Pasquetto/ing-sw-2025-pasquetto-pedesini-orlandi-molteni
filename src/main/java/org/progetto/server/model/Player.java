package org.progetto.server.model;

public class Player {

    // =======================
    // ATTRIBUTES
    // =======================

    String name;
    int credits;
    int color;
    int position;
    Spaceship spaceship;
    boolean hasLeft;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Player(String name, int color, int levelShip) {
        this.name = name;
        this.credits = 0;
        this.color = color;
        this.position = 0;
        this.spaceship = new Spaceship(levelShip, color);
        this.hasLeft = false;
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

    // =======================
    // SETTERS
    // =======================

    public void setPosition(int newPosition) {
        this.position = newPosition;
    }

    public void setHasLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * @param creditsToAdd is the number of credits gained (if positive) or lost (if negative)
     */
    public void addCredits(int creditsToAdd) {
        this.credits += creditsToAdd;
    }

    /**
     * @return the sum of the rolled
     */
    public int rollDice(){
        int die1 = (int) (Math.random() * 6) + 1;
        int die2 = (int) (Math.random() * 6) + 1;
        return die1 + die2;
    }
}