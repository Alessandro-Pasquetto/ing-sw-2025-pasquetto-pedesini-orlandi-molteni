package org.progetto.messages.toClient.Spaceship;

import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;

import java.io.Serializable;

public class UpdatedSpaceshipMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private Player player;
    private Component componentToUpdate;

    // =======================
    // CONSTRUCTORS
    // =======================

    public UpdatedSpaceshipMessage(Player player, Component componentToUpdate) {
        this.player = player;
        this.componentToUpdate = componentToUpdate;
    }

    // =======================
    // GETTERS
    // =======================
    public Player getPlayer() {
        return player;
    }

    public Component getComponentToUpdate() {
        return componentToUpdate;
    }


}