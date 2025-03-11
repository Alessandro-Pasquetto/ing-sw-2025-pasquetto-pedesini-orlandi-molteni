package org.progetto.server.model;
import java.util.ArrayList;
import org.progetto.server.model.components.Component;

public class BuildingBoard {

    // =======================
    // ATTRIBUTES
    // =======================

    private Component[][] spaceshipMatrix;  //composition of components
    private  int[][] BoardMask;   //mask layer for building clearance
    private ArrayList<Component> booked;  //list for booked components storage
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard( String imgSrc) {
        this.imgSrc = imgSrc;
        this.booked = new ArrayList<>(2);
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

    public int[][] getBoardMask() {
        return BoardMask;
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


    // =======================
    // OTHER METHODS
    // =======================

    /**
     * @author Lorenzo
     * @return the loaded matrix configuration for the board
     */
    int[][] loadBoardMask()
    {
        return BoardMask;
    }

    /**
     * @author Lorenzo
     * @return the component matrix for the spaceship initialized with null values
     */
    Component[][] createSpaceshipMatrix()
    {
        return spaceshipMatrix;
    }



}
