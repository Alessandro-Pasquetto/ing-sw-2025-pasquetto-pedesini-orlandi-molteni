package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class BatteryDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xBatteryStorage;
    private final int yBatteryStorage;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BatteryDiscardedMessage(int xBatteryStorage, int yBatteryStorage) {
        this.xBatteryStorage = xBatteryStorage;
        this.yBatteryStorage = yBatteryStorage;
    }

    // =======================
    // GETTERS
    // =======================

    public int getXBatteryStorage() {
        return xBatteryStorage;
    }

    public int getYBatteryStorage() {
        return yBatteryStorage;
    }
}
