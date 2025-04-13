package org.progetto.messages.toClient.Building;

import org.progetto.server.connection.Sender;
import org.progetto.server.model.components.Component;

import java.io.Serializable;

public class ShowHandComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private Component handComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ShowHandComponentMessage(Component handComponent) {
        this.handComponent = handComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public Component getHandComponent() {
        return handComponent;
    }
}
