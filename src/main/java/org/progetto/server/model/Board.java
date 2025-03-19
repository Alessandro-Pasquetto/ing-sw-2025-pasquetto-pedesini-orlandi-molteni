package org.progetto.server.model;

import java.util.ArrayList;

public class Board {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] track;
    private final ArrayList<Player> activeTravelers ;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Board(int sizeBoard, String imgSrc) {
        this.track = new Player[sizeBoard];
        this.activeTravelers = new ArrayList<>();
        this.imgSrc = imgSrc;
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getTrack() {
        return track;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * @author Alessandro
     * @param player is the new traveler
     */
    public synchronized void addTraveler(Player player) {
        activeTravelers.add(player);

        switch (activeTravelers.size()){
            case 1:
                track[4] = player;
                player.setPosition(4);
                break;
            case 2:
                track[2] = player;
                player.setPosition(2);
                break;
            case 3:
                track[1] = player;
                player.setPosition(1);
                break;
            case 4:
                track[0] = player;
                player.setPosition(0);
                break;
        }
    }

    /**
     * @author Alessandro
     * @param player the moving player
     * @param distance distance traveled by the player
     */
    public synchronized void movePlayerByDistance(Player player, int distance) {
        int sign;
        if(distance < 0){
            sign = -1;
            distance *= -1;
        }
        else
            sign = 1;

        int playerPosition = player.getPosition();

        while(distance != 0) {
            playerPosition += sign;

            if(track[playerPosition % track.length] == null)
                distance--;
        }

        track[playerPosition % track.length] = player;
        player.setPosition(playerPosition);
    }

    /**
     * @author Alessandro
     * @param player is the player who leaves the travel
     */
    public void leaveTravel(Player player) {
        int playerPosition = player.getPosition();
        track[playerPosition % track.length] = null;
        player.setPosition(-1);
    }
}