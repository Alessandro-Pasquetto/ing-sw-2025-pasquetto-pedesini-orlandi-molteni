package org.progetto.messages.toClient;

import java.io.Serializable;

public class HowManyDoubleCannonsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int maxUsable;
    int firePowerRequired;

    // =======================
    // CONSTRUCTORS
    // =======================

    public HowManyDoubleCannonsMessage(int maxUsable, int firePowerRequired) {
        this.maxUsable = maxUsable;
        this.firePowerRequired = firePowerRequired;
    }

    // =======================
    // GETTERS
    // =======================

    public int getMaxUsable() {
        return maxUsable;
    }
    public int getFirePowerRequired() {return firePowerRequired;}
}