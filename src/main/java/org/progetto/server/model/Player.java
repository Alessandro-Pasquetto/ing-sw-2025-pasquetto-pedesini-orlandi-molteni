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

    // =======================
    // CONSTRUCTORS
    // =======================

    public Player(String name, int color, int levelShip) {
        this.name = name;
        this.credits = 0;
        this.color = color;
        this.position = 0;
        this.spaceship = new Spaceship(levelShip);
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

    // =======================
    // SETTERS
    // =======================

    public void setPosition(int newPosition) {
        this.position = newPosition;
    }

    public void addCredits(int creditsToAdd) {
        this.credits += creditsToAdd;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public int throwDice(){return 0;}

    public void leaveTravel(){}

}