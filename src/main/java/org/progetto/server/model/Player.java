package org.progetto.server.model;

public class Player {
    private String username;
    private int credits = 0;

    public Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
