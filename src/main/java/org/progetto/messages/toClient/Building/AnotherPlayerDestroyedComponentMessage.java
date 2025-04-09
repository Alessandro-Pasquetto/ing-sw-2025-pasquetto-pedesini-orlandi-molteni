package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerDestroyedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final String namePlayer;
    private final int xComponent;
    private final int yComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDestroyedComponentMessage(String namePlayer, int xComponent, int yComponent) {
        this.namePlayer = namePlayer;
        this.xComponent = xComponent;
        this.yComponent = yComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }

    public int getyComponent() {
        return yComponent;
    }

    public int getxComponent() {
        return xComponent;
    }
}
