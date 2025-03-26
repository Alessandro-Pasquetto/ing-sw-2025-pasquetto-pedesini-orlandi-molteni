package org.progetto.messages.toClient;

import org.progetto.server.controller.GameManager;
import org.progetto.server.model.Player;

import java.io.Serializable;

public class GameInfoMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String imgPathBoard;
    private String imgPathSpaceship;
    private String imgPathCentralUnit;

    // =======================
    // CONSTRUCTORS
    // =======================
    public GameInfoMessage(String imgPathBoard, String imgPathSpaceship, String imgPathCentralUnit) {
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