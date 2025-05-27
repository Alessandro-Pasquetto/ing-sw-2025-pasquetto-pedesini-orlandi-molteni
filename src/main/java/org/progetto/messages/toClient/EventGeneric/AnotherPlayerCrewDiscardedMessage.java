package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerCrewDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;
    private int xHousingUnit;
    private int yHousingUnit;

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
