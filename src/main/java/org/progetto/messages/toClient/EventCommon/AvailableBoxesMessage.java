package org.progetto.messages.toClient.EventCommon;

import org.progetto.server.model.components.Box;

import java.io.Serializable;
import java.util.ArrayList;

public class AvailableBoxesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    ArrayList<Box> boxes;

    // =======================
    // CONSTRUCTORS
    // =======================
    public AvailableBoxesMessage(ArrayList<Box> boxes) {
        this.boxes = boxes;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Box> getBoxes() {
        return boxes;
    }


}