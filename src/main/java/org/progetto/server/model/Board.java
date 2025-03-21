package org.progetto.server.model;

import java.util.ArrayList;
import java.util.Comparator;

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

    public Board(int levelBoard) {
        this.track = new Player[elaborateSizeBoardFromLv(levelBoard)];
        this.activeTravelers = new ArrayList<>();
        this.imgSrc = "Board" + levelBoard + ".png";
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
     * @return the board size
     */
    private int elaborateSizeBoardFromLv(int levelBoard) {
        return switch (levelBoard) {
            case 1 -> 18;
            case 2 -> 24;
            case 3 -> 34;
            default -> 0;
        };
    }

    /**
     * @param player is the new traveler
     */
    public synchronized void addTraveler(Player player, int levelBoard) {
        activeTravelers.add(player);

        int pos = 0;
        switch (activeTravelers.size()){
            case 1:
                if(levelBoard == 1)
                    pos = 4;
                else if(levelBoard == 2)
                    pos = 6;
                break;
            case 2:
                if(levelBoard == 1)
                    pos = 2;
                else if(levelBoard == 2)
                    pos = 3;
                break;
            case 3:
                pos = 1;
                break;
            case 4:
                pos = 0;
                break;
        }

        track[pos] = player;
        player.setPosition(pos);
    }

    /**
     * @param player the moving player
     * @param distance distance traveled by the player
     */
    public synchronized void movePlayerByDistance(Player player, int distance) {
        int sign;
        int playerPosition = player.getPosition();

        track[playerPosition % track.length] = null;  // removes player from starting cell

        if(distance < 0){
            sign = -1;
        } else {
            sign = 1;
        }

        distance = Math.abs(distance);

        while(distance != 0) {
            playerPosition += sign;

            if(track[modulus(playerPosition, track.length)] == null)
                distance--;
        }

        track[modulus(playerPosition, track.length)] = player;
        player.setPosition(playerPosition);
    }

    private int modulus(int a, int b) {
        int result = a % b;
        return (result < 0) ? result + b : result;
    }

    public void updateTurnOrder(){
        activeTravelers.sort(Comparator.comparingInt(Player::getPosition));
    }

    /**
     * @param player is the player who leaves the travel
     */
    public void leaveTravel(Player player) {
        int playerPosition = player.getPosition();
        track[playerPosition % track.length] = null;
        player.setHasLeft(true);
    }
}