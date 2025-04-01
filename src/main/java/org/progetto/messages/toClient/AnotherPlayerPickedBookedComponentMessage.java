package org.progetto.messages.toClient;

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
    public AnotherPlayerPickedBookedComponentMessage(int idx, String name, String imgSrc) {
        this.idx = idx;
        this.name = name;
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