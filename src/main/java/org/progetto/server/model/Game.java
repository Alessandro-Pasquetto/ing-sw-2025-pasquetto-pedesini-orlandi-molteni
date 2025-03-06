package org.progetto.server.model;

import java.util.ArrayList;

public class Game {
    private int id;
    private ArrayList<Player> players = new ArrayList<>();

    public Game(int idGame) {
        this.id = idGame;
    }

    public void addPlayer(Player player) {
        synchronized (players) {
            players.add(player);
        }
    }

    public boolean tryAddPlayer(String username) {
        boolean avaiableUsername = true;

        synchronized (players) {
            for (Player p : players) {
                if (p.getUsername().equals(username)) {
                    avaiableUsername = false;
                    break;
                }
            }
        }
        return avaiableUsername;
    }
}