package org.progetto.messages.toClient.EventGeneric;

import org.progetto.server.model.components.Box;

import java.io.Serializable;

public class BoxAddedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;
    private final int xBoxStorage;
    private final int yBoxStorage;
    private final int boxIdx;
    private final Box box;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxAddedMessage(String playerName, int xBoxStorage, int yBoxStorage, int boxIdx, Box box) {
        this.playerName = playerName;
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.boxIdx = boxIdx;
        this.box = box;
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

    public Box getBox() {
        return box;
    }
}