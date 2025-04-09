package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class DestroyedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int xComponent;
    private int yComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public DestroyedComponentMessage(int xComponent, int yPlaceComponent) {
        this.xComponent = xComponent;
        this.yComponent = yComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public int getxComponent() {
        return xComponent;
    }

    public int getyComponent() {
        return yComponent;
    }
}
