package org.progetto.messages;

import java.io.Serializable;

public class AnotherPlayerPlacedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    String namePlayer;
    int xPlacedComponent;
    int yPlacedComponent;
    int rPlacedComponent;
    String imgSrcPlacedComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPlacedComponentMessage(String namePlayer, int xPlacedComponent, int yPlacedComponent, int rPlacedComponent, String imgSrcPlacedComponent) {
        this.namePlayer = namePlayer;
        this.xPlacedComponent = xPlacedComponent;
        this.yPlacedComponent = yPlacedComponent;
        this.rPlacedComponent = rPlacedComponent;
        this.imgSrcPlacedComponent = imgSrcPlacedComponent;
    }


    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getxPlacedComponent() {
        return xPlacedComponent;
    }

    public int getyPlacedComponent() {
        return yPlacedComponent;
    }

    public int getrPlacedComponent() {
        return rPlacedComponent;
    }

    public String getImgSrcPlacedComponent() {
        return imgSrcPlacedComponent;
    }
}