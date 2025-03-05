package org.progetto.server;

public class Player {
    private String username;
    private int credits = 0;

    Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
