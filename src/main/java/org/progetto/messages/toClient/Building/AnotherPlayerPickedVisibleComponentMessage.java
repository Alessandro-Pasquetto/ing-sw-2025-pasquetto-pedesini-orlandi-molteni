package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;

import java.io.Serializable;

public class AnotherPlayerPickedVisibleComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String playerName;
    private final Component component;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPickedVisibleComponentMessage(String playerName, Component component) {
        this.playerName = playerName;
        this.component = component;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public Component getPickedComponent() {
        return component;
    }
}
