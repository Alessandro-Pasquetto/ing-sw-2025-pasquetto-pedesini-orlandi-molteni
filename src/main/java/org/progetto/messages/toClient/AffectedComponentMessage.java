package org.progetto.messages.toClient;

import java.io.Serializable;

public class AffectedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int xComponent;
    private int yComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AffectedComponentMessage(int xComponent, int yComponent) {
        this.xComponent = xComponent;
        this.yComponent = yComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public int getXComponent() {
        return xComponent;
    }

    public int getYComponent() {
        return yComponent;
    }
}
