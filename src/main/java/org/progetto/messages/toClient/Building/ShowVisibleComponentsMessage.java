package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;

import java.io.Serializable;
import java.util.ArrayList;

public class ShowVisibleComponentsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Component> visibleComponentDeck;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowVisibleComponentsMessage(ArrayList<Component> visibleComponentDeck) {
        this.visibleComponentDeck = visibleComponentDeck;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Component> getVisibleComponentDeck() {
        return visibleComponentDeck;
    }
}
