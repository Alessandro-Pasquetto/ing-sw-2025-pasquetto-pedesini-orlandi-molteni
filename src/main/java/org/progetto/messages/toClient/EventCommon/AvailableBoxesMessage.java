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
        this.boxes = boxes;
    }

    // =======================
    // GETTERS
    // =======================

    public String[] getBoxes() {
        String[] out = new String[boxes.size()];
        for(int i = 0; i < boxes.size(); i++) {
            out[i] = boxes.get(i).toString();
        }
        return out;
    }


}