package org.progetto.messages.toClient.Planets;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class AnotherPlayerLandedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================
    private final Player player;
    private final int planetIdx;


    // =======================
    // CONSTRUCTORS
    // =======================
    public AnotherPlayerLandedMessage(Player player, int planetIdx) {
        this.player = player;
        this.planetIdx = planetIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public Player getPlayer() {
        return player;
    }
    public String getPlayerName() {
        return player.getName();
    }

    public int getPlayerColor(){
        return player.getColor();
    }

    public int getPlanetIdx() {
        return planetIdx;
    }




}