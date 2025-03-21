package org.progetto.server.model.components;

public class Box {

    // =======================
    // ATTRIBUTES
    // =======================

    private BoxType type;
    private int value;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Box(BoxType type,int value) {
        this.type = type;
        this.value = value;
    }

    // =======================
    // GETTERS
    // =======================

    public BoxType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
