package org.progetto.server.model.components;

public class Box {

    // =======================
    // ATTRIBUTES
    // =======================

    private BoxType type;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Box(BoxType type) {
        this.type = type;
    }

    // =======================
    // GETTERS
    // =======================

    public BoxType getType() {
        return type;
    }
}
