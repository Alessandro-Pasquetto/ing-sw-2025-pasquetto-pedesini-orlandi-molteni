package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseBatteryToDiscardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xBatteryStorage;
    private final int yBatteryStorage;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseBatteryToDiscardMessage(int xBatteryStorage, int yBatteryStorage) {
        this.xBatteryStorage = xBatteryStorage;
        this.yBatteryStorage = yBatteryStorage;
    }

    // =======================
    // GETTERS
    // =======================

    public int getXBatteryStorage() {
        return xBatteryStorage;
    }

    public int getYBatteryStorage() { return yBatteryStorage; }
}
