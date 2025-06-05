package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class HowManyDoubleEnginesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int maxUsable;
    private final int enginePower;

    // =======================
    // CONSTRUCTORS
    // =======================

    public HowManyDoubleEnginesMessage(int maxUsable, int enginePower) {
        this.maxUsable = maxUsable;
        this.enginePower = enginePower;
    }

    // =======================
    // GETTERS
    // =======================

    public int getMaxUsable() {
        return maxUsable;
    }
    public int getEnginePower() {return enginePower; }
}
