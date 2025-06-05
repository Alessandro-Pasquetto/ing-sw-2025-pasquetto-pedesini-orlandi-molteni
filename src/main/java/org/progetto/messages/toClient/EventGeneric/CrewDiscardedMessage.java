package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class CrewDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xHousingUnit;
    private final int yHousingUnit;

    // =======================
    // CONSTRUCTORS
    // =======================

    public CrewDiscardedMessage(int xHousingUnit, int yHousingUnit) {
        this.xHousingUnit = xHousingUnit;
        this.yHousingUnit = yHousingUnit;
    }

    // =======================
    // GETTERS
    // =======================

    public int getXHousingUnit() {
        return xHousingUnit;
    }

    public int getYHousingUnit() {
        return yHousingUnit;
    }
}
