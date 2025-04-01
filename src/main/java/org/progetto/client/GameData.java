package org.progetto.client;

/**
 * Client data useful to track game evolution
 */
public class GameData {

    // =======================
    // ATTRIBUTES
    // =======================

    // Client data that I want to store
    private static int idGame;
    private static String namePlayer;
    private static boolean timerExpired;

    // Temporary data of the handComponent in view
    private static int rHandComponent = 0;
    private static int xHandComponent = -1;
    private static int yHandComponent = -1;

    // =======================
    // GETTERS
    // =======================

    public static int getIdGame() {
        return idGame;
    }

    public static String getNamePlayer(){
        return namePlayer;
    }

    public static boolean getTimerExpired(){
        return timerExpired;
    }

    public static int getrHandComponent(){
        return rHandComponent;
    }

    public static int getxHandComponent(){
        return xHandComponent;
    }

    public static int getyHandComponent(){
        return yHandComponent;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setIdGame(int idGame) {
        GameData.idGame = idGame;
    }

    public static void setNamePlayer(String namePlayer){
        GameData.namePlayer = namePlayer;
    }

    public static void setTimerExpired(boolean timerExpired){
        GameData.timerExpired = timerExpired;
    }

    public static void setxHandComponent(int xHandComponent){
        GameData.xHandComponent = xHandComponent;
    }

    public static void setyHandComponent(int yHandComponent){
        GameData.yHandComponent = yHandComponent;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void rotateComponent(){
        if(rHandComponent == 3)
            rHandComponent = 0;
        else
            rHandComponent++;
    }

    public static void resetHandComponent(){
        xHandComponent = -1; // If it has not been placed in the matrix yet
        yHandComponent = -1;
        rHandComponent = 0;
    }
}