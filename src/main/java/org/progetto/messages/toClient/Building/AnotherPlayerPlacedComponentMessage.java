package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.io.Serializable;

public class AnotherPlayerPlacedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private Component component;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPlacedComponentMessage(String namePlayer,Component component) {
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