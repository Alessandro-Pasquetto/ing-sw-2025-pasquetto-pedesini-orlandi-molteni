package org.progetto.messages.toServer;

import java.io.Serializable;

public class MoveBoxMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int xStart;
    private int yStart;
    private int idxStart;
    private int xDestination;
    private int yDestination;
    private int idxDestination;

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
