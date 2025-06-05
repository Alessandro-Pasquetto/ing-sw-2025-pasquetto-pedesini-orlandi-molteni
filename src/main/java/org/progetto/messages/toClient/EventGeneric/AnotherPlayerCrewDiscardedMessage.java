package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerCrewDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;
    private final int xHousingUnit;
    private final int yHousingUnit;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerCrewDiscardedMessage(String playerName, int xHousingUnit, int yHousingUnit) {
        this.playerName = playerName;
        this.xHousingUnit = xHousingUnit;
        this.yHousingUnit = yHousingUnit;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public int getXHousingUnit() {
        return xHousingUnit;
    }

    public int getYHousingUnit() {
        return yHousingUnit;
    }
}
