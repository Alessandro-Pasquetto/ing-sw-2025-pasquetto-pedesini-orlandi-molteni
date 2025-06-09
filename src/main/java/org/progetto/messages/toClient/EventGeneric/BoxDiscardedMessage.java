package org.progetto.messages.toClient.EventGeneric;

import java.io.Serializable;

public class BoxDiscardedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xBoxStorage;
    private final int yBoxStorage;
    private final int boxIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxDiscardedMessage(int xBoxStorage, int yBoxStorage, int boxIdx) {
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.boxIdx = boxIdx;
    }

    // =======================
    // GETTERS
    // =======================

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
