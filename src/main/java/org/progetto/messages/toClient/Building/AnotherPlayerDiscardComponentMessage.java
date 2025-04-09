package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerDiscardComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int xPlacedComponent;
    private int yPlacedComponent;
    private int rPlacedComponent;
    private String imgSrcDiscardedComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDiscardComponentMessage(String namePlayer, String imgSrcDiscardedComponent) {
        this.namePlayer = namePlayer;
        this.imgSrcDiscardedComponent = imgSrcDiscardedComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public String getImgSrcDiscardedComponent() {
        return imgSrcDiscardedComponent;
    }
}