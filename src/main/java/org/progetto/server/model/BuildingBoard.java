package org.progetto.server.model;
import java.util.ArrayList;

import org.progetto.server.model.loadClasses.MaskMatrix;
import org.progetto.server.model.components.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;



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

    public int[][] getBoardMask() {
        return boardMask;
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

    /**
     * @author Lorenzo
     * @param x coordinate for placing component
     * @param y coordinate for placing component
     * @return true if component has been placed correctly else otherwise
     */
    public boolean placeComponent(int x, int y) {
        return false;

    }

    public void setHandComponent(Component component) {
        this.handComponent = component;
    }


    // =======================
    // OTHER METHODS
    // =======================

    /**
     * @author Lorenzo
     * @param levelShip is the game level chosen
     * @return the loaded matrix configuration for the board
     */
    private int[][] loadBoardMask(int levelShip)
    {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            MaskMatrix data = objectMapper.readValue(new File("src/main/resources/org.progetto.server/Masks.json"), MaskMatrix.class);

            switch (levelShip)
            {
                case 1:
                    boardMask = data.getBaseMatrix();
                    break;

                case 2:
                    boardMask = data.getAdvancedMatrix();
                    break;

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return boardMask;
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
