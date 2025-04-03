package org.progetto.messages.toClient;

import java.io.Serializable;

public class DestroyedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int yComponent;
    private int xComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public DestroyedComponentMessage(int yComponent, int xComponent) {
        this.yComponent = yComponent;
        this.xComponent = xComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public int getyComponent() {
        return yComponent;
    }

    public int getxComponent() {
        return xComponent;
    }
}
