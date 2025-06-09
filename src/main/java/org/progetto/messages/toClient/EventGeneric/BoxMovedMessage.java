package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class BoxMovedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String playerName;
    private final int xBoxStorage_start;
    private final int yBoxStorage_start;
    private final int boxIdx_start;
    private final int xBoxStorage_end;
    private final int yBoxStorage_end;
    private final int boxIdx_end;

    public BoxMovedMessage(String playerName, int xBoxStorageStart, int yBoxStorageStart, int boxIdxStart, int xBoxStorageEnd, int yBoxStorageEnd, int boxIdxEnd) {
        this.playerName = playerName;
        xBoxStorage_start = xBoxStorageStart;
        yBoxStorage_start = yBoxStorageStart;
        boxIdx_start = boxIdxStart;
        xBoxStorage_end = xBoxStorageEnd;
        yBoxStorage_end = yBoxStorageEnd;
        boxIdx_end = boxIdxEnd;
    }

    // =======================
    // CONSTRUCTORS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public int getXBoxStorage_start() {
        return xBoxStorage_start;
    }

    public int getYBoxStorage_start() {
        return yBoxStorage_start;
    }

    public int getBoxIdx_start() {
        return boxIdx_start;
    }

    public int getXBoxStorage_end() {
        return xBoxStorage_end;
    }

    public int getYBoxStorage_end() {
        return yBoxStorage_end;
    }

    public int getBoxIdx_end() {
        return boxIdx_end;
    }
}