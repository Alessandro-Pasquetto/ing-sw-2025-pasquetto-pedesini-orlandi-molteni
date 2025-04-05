package org.progetto.server.model;

import java.util.ArrayList;
import java.util.Comparator;

public class Board {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] track;
    private final ArrayList<Player> activePlayers;  // order: leader -> last
    private final ArrayList<Player> readyPlayers;   // order: first player ready -> last one
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Board(int levelBoard) {
        this.track = new Player[elaborateSizeBoardFromLv(levelBoard)];
        this.activePlayers = new ArrayList<>();
        this.readyPlayers = new ArrayList<>();
        this.imgSrc = "board" + levelBoard + ".png";
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getTrack() {
        return track;
    }

    public ArrayList<Player> getActivePlayers() {
        return activePlayers;
    }

    public ArrayList<Player> getReadyPlayers() {
        return readyPlayers;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Returns the size of the board based on the game level
     *
     * @author Alessandro
     * @return the board size
     */
    private int elaborateSizeBoardFromLv(int levelBoard) {
        return switch (levelBoard) {
            case 1 -> 18;
            case 2 -> 24;
            default -> 0;
        };
    }

    /**
     * Adds a player to the list of ready ones
     *
     * @author Gabriele
     * @param player reference to ready player
     */
    public synchronized void addReadyTraveler(Player player) {
        if (readyPlayers.contains(player)){
            throw new IllegalStateException("PlayerIsAlreadyReady");
        }

        readyPlayers.add(player);
    }

    /**
     * Adds a player to the list of players participating in the journey
     *
     * @author Alessandro
     * @param player is the new traveler
     */
    public synchronized void addTraveler(Player player, int levelBoard) {
        activePlayers.add(player);

        int pos = 0;
        switch (activePlayers.size()){
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
     * Moves the player forward on the board
     *
     * @author Alessandro
     * @param player the moving player
     * @param distance distance traveled by the player
     */
    public synchronized void movePlayerByDistance(Player player, int distance) throws IllegalStateException {
        int sign;
        int playerPosition = player.getPosition();

        track[modulus(playerPosition, track.length)] = null;  // removes player from starting cell

        if(distance < 0)
            sign = -1;
        else
            sign = 1;

        distance = Math.abs(distance);

        while(distance != 0) {
            playerPosition += sign;

            Player trackCell = track[modulus(playerPosition, track.length)];

            if(trackCell == null) {
                distance--;
            } else {
                // TODO: rivedere la logica di lapping
                if(trackCell.getPosition() <= playerPosition - track.length) {
                    leaveTravel(trackCell);
                    throw new IllegalStateException("PlayerLapped");
                }
            }
        }

        track[modulus(playerPosition, track.length)] = player;
        player.setPosition(playerPosition);
    }

    /**
     * Calculate the modulus
     *
     * @author Alessandro
     * @param a the dividend
     * @param b the divisor
     * @return the modulus
     */
    private int modulus(int a, int b) {
        int result = a % b;
        return (result < 0) ? result + b : result;
    }

    /**
     * Sort the active players on their position in the track, from the leader to the rest
     *
     * @author Alessandro
     */
    public void updateTurnOrder() {
        activePlayers.sort(Comparator.comparingInt(Player::getPosition).reversed());
    }

    /**
     * Makes a player abandon the journey
     *
     * @author Alessandro
     * @param player is the player who leaves the travel
     */
    public void leaveTravel(Player player) {
        int playerPosition = player.getPosition();
        track[modulus(playerPosition, track.length)] = null;
        activePlayers.remove(player);
        player.setHasLeft(true);
    }
}