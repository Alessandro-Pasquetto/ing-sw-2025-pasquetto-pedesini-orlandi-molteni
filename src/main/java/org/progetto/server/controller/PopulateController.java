package org.progetto.server.controller;

import org.progetto.messages.toClient.AskAlien;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

public class PopulateController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void askAliens(GameManager gameManager) throws InterruptedException, RemoteException {

        Game game = gameManager.getGame();

        askPurpleAlien(gameManager);
    }

    private static void askPurpleAlien(GameManager gameManager) throws RemoteException {
        for (Player player : gameManager.getGame().getBoard().getCopyTravelers()){
            if (!player.getIsReady() && player.getSpaceship().checkShipAllowPurpleAlien()){

                Sender sender = gameManager.getSenderByPlayer(player);
                sender.sendMessage(new AskAlien("purple", player.getSpaceship()));
            }
        }
    }

    private static void askOrangeAlien(GameManager gameManager) throws RemoteException {
        for (Player player : gameManager.getGame().getBoard().getCopyTravelers()){
            if (!player.getIsReady() && player.getSpaceship().checkShipAllowOrangeAlien()){

                Sender sender = gameManager.getSenderByPlayer(player);
                sender.sendMessage(new AskAlien("orange", player.getSpaceship()));
            }
        }
    }

    public static void receivePlaceAlien(GameManager gameManager, Player player, int x, int y, String color) throws RemoteException {
        if(color.equals("purple"))
            receivePurpleAlien(gameManager, player, x, y);
        else
            receiveOrangeAlien(gameManager, player, x, y);
    }

    private static void receivePurpleAlien(GameManager gameManager, Player player, int x, int y) throws RemoteException {
        Game game = gameManager.getGame();

        SpaceshipController.populateAlienComponent(gameManager, player, "purple", x, y, gameManager.getSenderByPlayer(player));

        player.setIsReady(true, game);
        gameManager.getGameThread().notifyThread();
    }

    private static void receiveOrangeAlien(GameManager gameManager, Player player, int x, int y) throws RemoteException {
        Game game = gameManager.getGame();

        SpaceshipController.populateAlienComponent(gameManager, player, "orange", x, y, gameManager.getSenderByPlayer(player));

        player.setIsReady(true, game);
        gameManager.getGameThread().notifyThread();
    }
}