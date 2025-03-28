package org.progetto.messages.toClient;

public class AnotherPlayerDiscardComponentMessage {

// =======================
    // ATTRIBUTES
    // =======================

    String namePlayer;
    int xPlacedComponent;
    int yPlacedComponent;
    int rPlacedComponent;
    String imgSrcDiscardedComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDiscardComponentMessage(String namePlayer,String imgSrcDiscardedComponent) {
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