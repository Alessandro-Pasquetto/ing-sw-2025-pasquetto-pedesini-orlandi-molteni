package org.progetto.server.model.components;

import java.io.Serializable;

public class Component implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    protected final ComponentType type;
    private final int[] connections;   // {0, 1, 2, 3} = {n°up, n°right, n°down, n°left}
    private int rotation;              // {0, 1, 2, 3} = {up, right, down, left} for shields {left-up, up-right, right-down, down-left}
    private int xCoordinate;           // x coordinate in matrix
    private int yCoordinate;           // y coordinate in matrix
    private final String imgSrc;
    private boolean hasBeenBooked;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Component(ComponentType type, int[] connections, String imgSrc) {
        this.type = type;
        this.connections = connections;
        this.rotation = 0;
        this.imgSrc = imgSrc;
        this.hasBeenBooked = false;
    }

    // =======================
    // GETTERS
    // =======================

    public ComponentType getType() {
        return type;
    }

    public int[] getConnections() {
        return connections;
    }

    public int getRotation() {
        return rotation;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public int getX() {
        return xCoordinate;
    }

    public int getY() {
        return yCoordinate;
    }

    public boolean getHasBeenBooked(){
        return hasBeenBooked;
    }

    // =======================
    // SETTERS
    // =======================

    public void setRotation(int rotation) {
        for (int i = 0; i < rotation; i++) {
            rotate();
        }
    }

    public void setX(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public void setY(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public void setHasBeenBooked(boolean hasBeenBooked) {
        this.hasBeenBooked = hasBeenBooked;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Rotates of ninety degrees the component (clockwise)
     *
     * @author Lorenzo
     */
    public void rotate() {
        if (rotation == 3)
            rotation = 0;
        else
            rotation++;

        int saveLast = connections[3];
        for (int i = 3; i > 0; i--)
            connections[i] = connections[i - 1];
        connections[0] = saveLast;
    }
}