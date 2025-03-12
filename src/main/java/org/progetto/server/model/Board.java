package org.progetto.server.model;

public class Board {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] truck;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Board(int sizeBoard, String imgSrc) {
        this.truck = new Player[sizeBoard];
        this.imgSrc = imgSrc;
    }

    // =======================
    // GETTERS
    // =======================

    public Player[] getTruck() {
        return truck;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // SETTERS
    // =======================

    private void setPlayerInTruck(Player player, int index) {
        truck[index] = player;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void movePlayerByDistance(Player player, int distance) {
        int playerPosition = player.getPosition();

        while(distance != 0) {
            playerPosition++;

            if(playerPosition == truck.length)
                playerPosition = 0;

            if(truck[playerPosition] == null)
                distance--;
        }

        setPlayerInTruck(player, playerPosition);
        player.setPosition(playerPosition);
    }
}