package org.progetto.messages;

import java.io.Serializable;

public class PickedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    String imgPath;

    // =======================
    // CONSTRUCTORS
    // =======================
    public PickedComponentMessage(String imgPath) {
        this.imgPath = imgPath;
    }

    // =======================
    // GETTERS
    // =======================

    public String getImgPath() {
        return imgPath;
    }
}