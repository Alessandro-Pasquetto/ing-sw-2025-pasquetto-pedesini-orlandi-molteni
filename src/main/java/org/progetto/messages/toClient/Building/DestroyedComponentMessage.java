package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class DestroyedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xComponent;
    private final int yComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public DestroyedComponentMessage(int xComponent, int yComponent) {
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
