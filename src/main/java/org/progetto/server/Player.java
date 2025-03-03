package org.progetto.server;

public class Player {
    private String username;
    private int credits;

    Player(String username, int credits) {
        this.username = username;
        this.credits = credits;
    }

    public String getUsername() {
        return username;
    }
}
