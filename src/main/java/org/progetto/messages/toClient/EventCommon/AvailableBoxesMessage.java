package org.progetto.messages.toClient.EventCommon;

import org.progetto.server.model.components.Box;
import java.io.Serializable;
import java.util.ArrayList;

public class AvailableBoxesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Integer> boxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AvailableBoxesMessage(ArrayList<Box> boxes) {

        this.boxes = new ArrayList<>();

        for (Box box : boxes) {
            this.boxes.add(box.getValue());
        }
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