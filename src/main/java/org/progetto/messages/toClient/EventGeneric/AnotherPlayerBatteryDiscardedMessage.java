package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerBatteryDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;
    private final int xBatteryStorage;
    private final int yBatteryStorage;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerBatteryDiscardedMessage(String namePlayer, int xBatteryStorage, int yBatteryStorage) {
        this.namePlayer = namePlayer;
        this.xBatteryStorage = xBatteryStorage;
        this.yBatteryStorage = yBatteryStorage;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getXBatteryStorage() {
        return xBatteryStorage;
    }

    public int getYBatteryStorage() {
        return yBatteryStorage;
    }
}
