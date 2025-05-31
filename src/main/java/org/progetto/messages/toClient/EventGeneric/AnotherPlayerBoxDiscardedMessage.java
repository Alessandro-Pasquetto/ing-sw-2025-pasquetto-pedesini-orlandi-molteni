package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerBoxDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;
    private int xBoxStorage;
    private int yBoxStorage;
    private int boxIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerBoxDiscardedMessage(String playerName, int xBoxStorage, int yBoxStorage, int boxIdx) {
        this.playerName = playerName;
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.boxIdx = boxIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public int getXBoxStorage() {
        return xBoxStorage;
    }

    public int getYBoxStorage() {
        return yBoxStorage;
    }

    public int getBoxIdx() {
        return boxIdx;
    }
}
