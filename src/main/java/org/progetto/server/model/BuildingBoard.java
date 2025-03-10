package org.progetto.server.model;
import java.util.ArrayList;
import org.progetto.server.model.components.Component;

public class BuildingBoard {

    // =======================
    // ATTRIBUTES
    // =======================

    private Component[][] spaceshipMatrix;
    private ArrayList<Component> booked;
    private Spaceship spaceship;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard(Component[][] spaceshipMatrix, String imgSrc) {
        this.spaceshipMatrix = spaceshipMatrix;
        this.imgSrc = imgSrc;
        this.booked = new ArrayList<>();
    }

    // =======================
    // GETTERS
    // =======================

    public Component[][] getSpaceshipMatrix() {
        return spaceshipMatrix;
    }

    public ArrayList<Component> getBooked() {
        return booked;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // SETTERS
    // =======================

    public boolean setAsBooked(Component component) {
        if (booked.size() < 2) {
            booked.add(component);  // need to handle booked flag in component
            return true;
        } else {
            return false;
        }
    }

    public void setSpaceship(Spaceship spaceship) {
        this.spaceship = spaceship;
    }
}
