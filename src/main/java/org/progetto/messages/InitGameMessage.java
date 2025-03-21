package org.progetto.messages;

import java.io.Serializable;

public class InitGameMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    String imgPathBoard;
    String imgPathSpaceship;
    String imgPathCentralUnit;

    // =======================
    // CONSTRUCTORS
    // =======================
    public InitGameMessage(String imgPathBoard, String imgPathSpaceship, String imgPathCentralUnit) {
        this.imgPathBoard = imgPathBoard;
        this.imgPathSpaceship = imgPathSpaceship;
        this.imgPathCentralUnit = imgPathCentralUnit;
    }

    // =======================
    // GETTERS
    // =======================
    public String getImgPathBoard() {
        return imgPathBoard;
    }

    public String getImgPathSpaceship() {
        return imgPathSpaceship;
    }

    public String getImgPathCentralUnit() {
        return imgPathCentralUnit;
    }
}