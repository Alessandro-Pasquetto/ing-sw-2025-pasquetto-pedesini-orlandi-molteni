package org.progetto.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Board {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] track;
    private final ArrayList<Player> travelers;  // order: leader -> last
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Board(int levelBoard) {
        this.track = new Player[elaborateSizeBoardFromLv(levelBoard)];
        this.travelers = new ArrayList<>();
        this.imgSrc = "board" + levelBoard + ".png";
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getTrack() {
        return track;
    }

    public ArrayList<Player> getCopyTravelers() {
        ArrayList<Player> copyTravelers;

        synchronized (travelers) {
            copyTravelers = new ArrayList<>(travelers);
        }
        return copyTravelers;
    }

    public int getNumTravelers() {
        return travelers.size();
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
    public synchronized void addTraveler(Player player) {

        if (!travelers.contains(player))
            travelers.add(player);
    }

    /**
     * Adds ready players to the list of players participating in the journey
     *
     * @author Alessandro
     * @param player reference to ready player
     */
    public synchronized void removeTraveler(Player player) {
        travelers.remove(player);
    }

    /**
     * Checks if all travelers are ready
     *
     * @author Alessandro
     * @return true if travelers are all ready; otherwise false
     */
    public synchronized boolean allTravelersReady() {
        for (Player traveler : travelers){
            if(!traveler.getIsReady())
                return false;
        }

        return true;
    }

    /**
     * Adds ready players to the list of players participating in the journey
     *
     * @author Alessandro
     * @param levelBoard
     */
    public synchronized void addTravelersInTrack(int levelBoard) {

        switch (levelBoard) {
            case 1:

                track[4] = travelers.get(0);
                travelers.get(0).setPosition(4);
                if(travelers.size() == 1) break;

                track[2] = travelers.get(1);
                travelers.get(1).setPosition(2);
                if(travelers.size() == 2) break;

                track[1] = travelers.get(2);
                travelers.get(2).setPosition(1);
                if(travelers.size() == 3) break;

                track[0] = travelers.get(3);
                travelers.get(3).setPosition(0);

                break;
            case 2:
                track[6] = travelers.get(0);
                travelers.get(0).setPosition(6);
                if(travelers.size() == 1) break;

                track[3] = travelers.get(1);
                travelers.get(1).setPosition(3);
                if(travelers.size() == 2) break;

                track[1] = travelers.get(2);
                travelers.get(2).setPosition(1);
                if(travelers.size() == 3) break;

                track[0] = travelers.get(3);
                travelers.get(3).setPosition(0);

                break;
        }
    }

    /**
     * Moves the player forward/backwards on the board
     *
     * @author Alessandro
     * @param player the moving player
     * @param distance distance traveled by the player
     */
    public synchronized void movePlayerByDistance(Player player, int distance) throws IllegalStateException {
        int sign;
        int playerPosition = player.getPosition();

        track[modulus(playerPosition, track.length)] = null;  // removes the current player from starting cell

        if (distance < 0)
            sign = -1;
        else
            sign = 1;

        distance = Math.abs(distance);

        while (distance != 0) {
            playerPosition += sign;
            int wrappedPosition = modulus(playerPosition, track.length);
            Player trackCell = track[wrappedPosition];

            if (trackCell == null) {
                distance--;
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
        travelers.sort(Comparator.comparingInt(Player::getPosition).reversed());
    }

    /**
     * Checks if there is any lapped player
     *
     * @author Gabriele
     * @return list of lapped players
     */
    public ArrayList<Player> checkLappedPlayers() {
        ArrayList<Player> lappedPlayers = new ArrayList<>();
        Player leader = travelers.getFirst();

        for (Player player : new ArrayList<>(travelers)) {
            if (leader.getPosition() >= player.getPosition() + track.length) {
                lappedPlayers.add(player);
                leaveTravel(player);
            }
        }

        return !lappedPlayers.isEmpty() ? lappedPlayers : null;
    }

    /**
     * Checks if there is any player with no crew
     *
     * @author Alessandro
     * @return list of players with no crew
     */
    public ArrayList<Player> checkNoCrewPlayers() {
        ArrayList<Player> noCrewPlayers = new ArrayList<>();

        for (Player player : new ArrayList<>(travelers)) {
            if (player.getSpaceship().getTotalCrewCount() == 0) {
                noCrewPlayers.add(player);
                leaveTravel(player);
            }
        }

        return !noCrewPlayers.isEmpty() ? noCrewPlayers : null;
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
        travelers.remove(player);
        player.setHasLeft(true);
    }
}