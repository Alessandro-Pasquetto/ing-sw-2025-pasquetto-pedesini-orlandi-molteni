package org.progetto.messages.toServer;

import java.io.Serializable;

public class RemoveBoxMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int xBoxStorage;
    private int yBoxStorage;
    private int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public RemoveBoxMessage(int xBoxStorage, int yBoxStorage, int idx) {
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getxBoxStorage() {
        return xBoxStorage;
    }

    public int getyBoxStorage() {
        return yBoxStorage;
    }

    public int getIdx() {
        return idx;
    }
}
