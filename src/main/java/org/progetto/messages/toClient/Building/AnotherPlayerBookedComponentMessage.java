package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;
import java.io.Serializable;

public class AnotherPlayerBookedComponentMessage implements Serializable {

    private final String playerName;
    private final Component component;
    private final int idx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerBookedComponentMessage(String playerName, Component component, int idx) {
        this.playerName = playerName;
        this.component = component;
        this.idx = idx;
    }

    // =======================
    // GETTERS
    // =======================

    public String getPlayerName() {
        return playerName;
    }

    public Component getComponent() {
        return component;
    }

    public int getIdx() {
        return idx;
    }
}