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
    boolean isReady;

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

    // =======================
    // SETTERS
    // =======================

    public void setPosition(int newPosition) {
        this.position = newPosition;
    }

    public void setHasLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    public void setIsReady(boolean isReady, Game game) {
        this.isReady = isReady;

        game.addReadyPlayers(isReady);
    }

    // =======================
    // OTHER METHODS
    // =======================

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