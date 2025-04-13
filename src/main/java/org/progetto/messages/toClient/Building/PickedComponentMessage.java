package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;
import java.io.Serializable;

public class PickedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Component component;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PickedComponentMessage(Component component) {
        this.component = component;
    }

    // =======================
    // GETTERS
    // =======================

    public Component getPickedComponent() {
        return component;
    }
}