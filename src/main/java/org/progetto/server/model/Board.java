package org.progetto.server.model;

public class Board {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Player[] truck;
    private final Timer timer;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Board(int sizeBoard, int defaultTimer, int timerFlipsAllowed, String imgSrc) {
        this.truck = new Player[sizeBoard];
        this.timer  = new Timer(defaultTimer, timerFlipsAllowed);
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