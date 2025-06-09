package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseRewardBoxMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int rewardIdxBox;
    private final int xBoxStorage;
    private final int yBoxStorage;
    private final int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseRewardBoxMessage(int rewardIdxBox, int xBoxStorage, int yBoxStorage, int idx) {
        this.rewardIdxBox = rewardIdxBox;
        this.xBoxStorage = xBoxStorage;
        this.yBoxStorage = yBoxStorage;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getRewardIdxBox() { return rewardIdxBox; }

    public int getXBoxStorage() { return xBoxStorage; }

    public int getYBoxStorage() { return yBoxStorage; }

    public int getIdx() { return idx; }
}