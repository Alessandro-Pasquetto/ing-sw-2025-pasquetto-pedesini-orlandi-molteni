package org.progetto.server.model;
import java.util.ArrayList;

import org.progetto.server.model.components.Component;

public class BuildingBoard {

    // =======================
    // ATTRIBUTES
    // =======================

    private Component[][] spaceshipMatrix;  //composition of components
    private int[][] boardMask;   //mask layer for building clearance
    private ArrayList<Component> booked;  //list for booked components storage
    private Component handComponent;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard(int levelShip) {
        this.spaceshipMatrix = createSpaceshipMatrix(levelShip);
        this.boardMask = loadBoardMask(levelShip);
        this.imgSrc = loadImgSrc(levelShip);
        this.booked = new ArrayList<>(2);
    }


    // =======================
    // GETTERS
    // =======================

    public Component getHandComponent() {
        return handComponent;
    }

    public Component[][] getSpaceshipMatrix() {
        return spaceshipMatrix;
    }

    public ArrayList<Component> getBooked() {
        return booked;
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

    // todo: place handComponent in spaceshipMatrix
    // do i need to check if it's possibile (with boardMask) or after send boardMask to the client than it's not possible from the view send a wrong coord for placing the component?
    public void placeComponent(int x, int y) {

    }

    public void setHandComponent(Component component) {
        this.handComponent = component;
    }


    // =======================
    // OTHER METHODS
    // =======================

    /**
     * @author Lorenzo
     * @return the loaded matrix configuration for the board
     */
    private int[][] loadBoardMask(int levelShip)
    {
        return new int[3][3];
    }

    private String loadImgSrc(int levelShip){
        return "imgPath";
    }

    /**
     * @author Lorenzo
     * @return the component matrix for the spaceship initialized with null values
     */
    private Component[][] createSpaceshipMatrix(int levelShip)
    {
        return new Component[3][3];
    }



}
