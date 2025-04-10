package org.progetto.messages.toServer;

import org.progetto.server.model.components.Box;

import java.io.Serializable;

public class ResponseRewardBoxMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int idxBox;
    private int xBoxStorage;
    private int yBoxStorage;
    private int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseRewardBoxMessage(int idxBox, int xBoxStorage, int yBoxStorage, int idx) {
        this.idxBox = idxBox;
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdxBox() { return idxBox; }

    public int getXBoxStorage() { return xBoxStorage; }

    public int getYBoxStorage() { return yBoxStorage; }

    public int getIdx() { return idx; }
}