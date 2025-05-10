package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;

import java.io.Serializable;

public class ShowBookedComponentsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Component[] bookedComponents;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowBookedComponentsMessage(Component[] bookedComponents) {
        this.bookedComponents = bookedComponents;
    }

    // =======================
    // GETTERS
    // =======================

    public Component[] getBookedComponents() {
        return bookedComponents;
    }
}
