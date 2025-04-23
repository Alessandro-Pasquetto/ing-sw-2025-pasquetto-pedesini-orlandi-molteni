package org.progetto.messages.toClient.EventCommon;

import org.progetto.server.model.components.Box;
import java.io.Serializable;
import java.util.ArrayList;

public class AvailableBoxesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Box> boxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AvailableBoxesMessage(ArrayList<Box> boxes) {
        this.boxes = new ArrayList<>(boxes);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Box> getBoxes() {
        return boxes;
    }
}