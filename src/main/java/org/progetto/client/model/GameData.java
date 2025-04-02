package org.progetto.client.model;

import javafx.scene.image.ImageView;
import org.progetto.client.gui.DragAndDrop;

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

    // =======================
    // GETTERS
    // =======================

    public static int getIdGame() {
        return idGame;
    }

    public static String getNamePlayer(){
        return namePlayer;
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
}