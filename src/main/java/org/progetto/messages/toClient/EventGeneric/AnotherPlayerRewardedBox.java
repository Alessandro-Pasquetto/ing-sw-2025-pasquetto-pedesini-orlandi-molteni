package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class AnotherPlayerRewardedBox implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;

    private final int xBoxStorage;

    private final int yBoxStorage;

    private final int boxIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerRewardedBox(String playerName, int xBoxStorage, int yBoxStorage, int boxIdx) {
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

    public int getxBoxStorage() {
        return xBoxStorage;
    }

    public int getyBoxStorage() {
        return yBoxStorage;
    }

    public int getBoxIdx() {
        return boxIdx;
    }
}