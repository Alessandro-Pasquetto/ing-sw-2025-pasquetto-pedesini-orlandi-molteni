package org.progetto.messages.toClient;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class AnotherPlayerDestroyedComponent implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int y;
    int x;
    Player player;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerDestroyedComponent(Player player, int y, int x) {
        this.player = player;
        this.y = y;
        this.x = x;
    }


    // =======================
    // GETTERS
    // =======================


    public Player getPlayer() {
        return player;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}