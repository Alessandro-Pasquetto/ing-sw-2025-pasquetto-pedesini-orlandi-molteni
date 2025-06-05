package org.progetto.messages.toServer;

import java.io.Serializable;

public class MoveBoxMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int xStart;
    private final int yStart;
    private final int idxStart;
    private final int xDestination;
    private final int yDestination;
    private final int idxDestination;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MoveBoxMessage(int xStart, int yStart, int idxStart, int xDestination, int yDestination, int idxDestination) {
        this.xStart = xStart;
        this.yStart = yStart;
        this.idxStart = idxStart;
        this.xDestination = xDestination;
        this.yDestination = yDestination;
        this.idxDestination = idxDestination;
    }

    // =======================
    // GETTERS
    // =======================

    public int getxStart() {
        return xStart;
    }

    public int getyStart() {
        return yStart;
    }

    public int getIdxStart() {
        return idxStart;
    }

    public int getxDestination() {
        return xDestination;
    }

    public int getyDestination() {
        return yDestination;
    }

    public int getIdxDestination() {
        return idxDestination;
    }
}
