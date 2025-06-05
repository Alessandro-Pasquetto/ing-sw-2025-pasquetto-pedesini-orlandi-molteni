package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseBoxToDiscardMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xBoxStorage;
    private final int yBoxStorage;
    private final int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseBoxToDiscardMessage(int xBoxStorage, int yBoxStorage, int idx) {
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getXBoxStorage() { return xBoxStorage; }

    public int getYBoxStorage() { return yBoxStorage; }

    public int getIdx() { return idx; }
}