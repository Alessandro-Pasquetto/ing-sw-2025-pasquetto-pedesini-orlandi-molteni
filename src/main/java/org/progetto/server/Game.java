package org.progetto.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Game {
    int id;
    private static ArrayList<Player> players = new ArrayList<>();

    public Game() {
        id = 0;
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