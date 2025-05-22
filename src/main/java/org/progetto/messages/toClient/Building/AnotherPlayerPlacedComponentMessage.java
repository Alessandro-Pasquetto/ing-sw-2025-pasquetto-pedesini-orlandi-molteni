package org.progetto.messages.toClient.Building;

import org.progetto.server.model.components.Component;
import java.io.Serializable;

public class AnotherPlayerPlacedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;
    private final Component component;
    private final int x;
    private final int y;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerPlacedComponentMessage(String namePlayer, Component component, int x, int y) {
        this.namePlayer = namePlayer;
        this.component = component;
        this.x = x;
        this.y = y;
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}