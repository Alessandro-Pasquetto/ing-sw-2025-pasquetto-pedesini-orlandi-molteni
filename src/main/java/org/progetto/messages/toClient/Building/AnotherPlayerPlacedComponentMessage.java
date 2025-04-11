package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;
import java.io.Serializable;

public class AnotherPlayerPlacedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;
    private final Component component;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPlacedComponentMessage(String namePlayer, Component component) {
        this.namePlayer = namePlayer;
        this.component = component;
    }


    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public Component getComponent() {
        return component;
    }

    public String getImgSrcPlacedComponent() {
        return component.getImgSrc();
    }
}