package org.progetto.messages.toClient;

import java.io.Serializable;

public class HowManyDoubleEnginesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int maxUsable;

    // =======================
    // CONSTRUCTORS
    // =======================

    public HowManyDoubleEnginesMessage(int maxUsable) {
        this.maxUsable = maxUsable;
    }

    // =======================
    // GETTERS
    // =======================

    public int getMaxUsable() {
        return maxUsable;
    }
}
