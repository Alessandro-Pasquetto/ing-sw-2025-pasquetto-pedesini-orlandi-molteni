package org.progetto.messages.toServer;

import org.progetto.server.model.components.Box;

import java.io.Serializable;

public class ResponseRewardBoxMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private Box box;
    private int xBoxStorage;
    private int yBoxStorage;
    private int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseRewardBoxMessage(Box box, int xBoxStorage, int yBoxStorage, int idx) {
        this.box = box;
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public Box getBox() { return box; }

    public int getXBoxStorage() { return xBoxStorage; }

    public int getYBoxStorage() { return yBoxStorage; }

    public int getIdx() { return idx; }
}