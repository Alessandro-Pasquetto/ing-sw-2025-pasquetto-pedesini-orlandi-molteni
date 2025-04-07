package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerPlacedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int xPlacedComponent;
    private int yPlacedComponent;
    private int rPlacedComponent;
    private String imgSrcPlacedComponent;

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