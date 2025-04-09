package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerBookedComponentMessage implements Serializable {

    private String namePlayer;
    private int bookedIndex;
    private String imgSrcBookedComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerBookedComponentMessage(String namePlayer,String imgSrcBookedComponent, int bookedIndex) {
        this.namePlayer = namePlayer;
        this.imgSrcBookedComponent = imgSrcBookedComponent;
        this.bookedIndex = bookedIndex;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }
    public String getImgSrcBookedComponent() {
        return imgSrcBookedComponent;
    }
    public int getBookedIndex() {
        return bookedIndex;
    }



}