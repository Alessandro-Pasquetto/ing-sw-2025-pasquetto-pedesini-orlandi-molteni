package org.progetto.messages.toClient.Planets;

import org.progetto.server.model.Player;

public class AnotherPlayerLandedMessage {

    // =======================
    // ATTRIBUTES
    // =======================
    Player player;
    int planetIdx;


    // =======================
    // CONSTRUCTORS
    // =======================
    public AnotherPlayerLandedMessage(Player player,int planetIdx) {
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