package org.progetto.server.model.components;

public class Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private ComponentType type;
    private int[] connections;
    private int rotation;
    private boolean hidden;
    private boolean chosen;
    private boolean positionLocked;
    private String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Component(ComponentType type, int[] connections, String imgSrc) {
        this.type = type;
        this.connections = connections;
        this.rotation = 0;
        this.hidden = false;
        this.chosen = false;
        this.positionLocked = false;
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

    public boolean isChosen() {
        return chosen;
    }

    public boolean isPositionLocked() {
        return positionLocked;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // SETTERS
    // =======================

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    public void setPositionLocked(boolean positionLocked) {
        this.positionLocked = positionLocked;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Rotates the component clockwise
    public void rotate() {
        if (this.rotation == 3) {
            this.rotation = 0;
        } else {
            this.rotation++;
        }
    }
}
