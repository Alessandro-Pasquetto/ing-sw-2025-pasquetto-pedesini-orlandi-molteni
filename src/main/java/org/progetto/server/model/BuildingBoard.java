package org.progetto.server.model;
import java.util.ArrayList;
import org.progetto.server.model.components.Component;

public class BuildingBoard {

    private Component[][] spaceshipMatrix;
    private ArrayList<Component> booked = new ArrayList<>();
    private Spaceship spaceship;
    private final String imgSrc;

    public BuildingBoard(Component[][] spaceshipMatrix, String imgSrc) {

        this.spaceshipMatrix = spaceshipMatrix;   //type of building matrix
        this.imgSrc = imgSrc;                     //img view path

    }

    // getter //
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


    // setter //
    public void setAsBooked(Component component) {
         booked.add(component);  //need to handle booked flag in component
    }

    public void setSpaceship(Spaceship spaceship) {
        this.spaceship = spaceship;
    }










}
