package org.progetto.messages.toClient.LostShip;

import org.progetto.server.model.Player;

import java.io.Serializable;

public class AnotherPlayerLandedMessage implements Serializable {


    // =======================
    // ATTRIBUTES
    // =======================

    Player player;


    // =======================
    // CONSTRUCTORS
    // =======================
    public AnotherPlayerLandedMessage(Player player) {
        this.player = player;
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



}