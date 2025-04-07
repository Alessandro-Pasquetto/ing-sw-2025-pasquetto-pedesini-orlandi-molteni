package org.progetto.messages.toClient.Building;

import java.io.Serializable;

public class AnotherPlayerDestroyedComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String namePlayer;
    private int yComponent;
    private int xComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDestroyedComponentMessage(String namePlayer, int yComponent, int xComponent) {
        this.namePlayer = namePlayer;
        this.yComponent = yComponent;
        this.xComponent = xComponent;
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
