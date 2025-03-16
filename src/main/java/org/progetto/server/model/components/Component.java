package org.progetto.server.model.components;

public class Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ComponentType type;
    private final int[] connections;   //{0,1,2,3} = {n°up,n°right,n°down,n°left}
    private int rotation;        //{0,1,2,3} = {up, right, down, left} for shields {left-up, up-right, right-down, down-left}
    private int x_coordinate;    //x coordinate in matrix
    private int y_coordinate;    //y coordinate in matrix
    private boolean hidden;      //false if it was discarded
    private boolean placed;      //true if it's in the spaceship matrix
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Component(ComponentType type, int[] connections, String imgSrc) {
        this.type = type;
        this.connections = connections;
        this.rotation = 0;
        this.hidden = false;
        this.placed = false;
        this.imgSrc = imgSrc;
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

    public boolean isHidden() {
        return hidden;
    }

    public boolean isPlaced() {
        return placed;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public int getX_coordinate() {
        return x_coordinate;
    }

    public int getY_coordinate() {
        return y_coordinate;
    }

    // =======================
    // SETTERS
    // =======================

    public void setRotation(int rotation) {
        for (int i = 0; i < rotation; i++) {
            rotate();
        }
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }

    public void setX_coordinate(int x_coordinate) {
        this.x_coordinate = x_coordinate;
    }

    public void setY_coordinate(int y_coordinate) {
        this.y_coordinate = y_coordinate;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Rotates the component clockwise
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