package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerPickedBookedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    String name;
    String imgSrc;
    int idx;

    // =======================
    // CONSTRUCTORS
    // =======================
    public AnotherPlayerPickedBookedComponentMessage(String name, int idx, String imgSrc) {
        this.name = name;
        this.idx = idx;
        this.imgSrc = imgSrc;
    }

    // =======================
    // GETTERS
    // =======================

    public String getName() {
        return name;
    }
    public String getImgSrc() {
        return imgSrc;
    }
    public int getIdx() {
        return idx;
    }
}