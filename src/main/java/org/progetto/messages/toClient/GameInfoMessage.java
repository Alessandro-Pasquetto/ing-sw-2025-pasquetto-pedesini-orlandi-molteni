package org.progetto.messages.toClient;

import java.io.Serializable;

public class GameInfoMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int idGame;
    private String imgPathBoard;
    private String imgPathSpaceship;
    private String imgPathCentralUnit;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameInfoMessage(int idGame, String imgPathBoard, String imgPathSpaceship, String imgPathCentralUnit) {
        this.idGame = idGame;
        this.imgPathBoard = imgPathBoard;
        this.imgPathSpaceship = imgPathSpaceship;
        this.imgPathCentralUnit = imgPathCentralUnit;
    }

    // =======================
    // GETTERS
    // =======================

    public int getIdGame() {
        return idGame;
    }

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