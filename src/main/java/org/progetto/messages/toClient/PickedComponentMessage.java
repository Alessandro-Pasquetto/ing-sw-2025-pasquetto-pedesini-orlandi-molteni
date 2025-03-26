package org.progetto.messages.toClient;

import java.io.Serializable;

public class PickedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String imgPath;

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