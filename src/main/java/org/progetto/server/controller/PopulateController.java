package org.progetto.server.controller;

import org.progetto.messages.toClient.AskAlien;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

public class PopulateController {

    // =======================
    // OTHER METHODS
    // =======================

    public static void askAliens(GameManager gameManager) throws InterruptedException, RemoteException {

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
        Sender sender = gameManager.getSenderByPlayer(player);

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POPULATING))) {
            sender.sendMessage("IncorrectPhase");
        }

        if(x == -1 && y == -1){
            askOrangeAlien(gameManager);
            return;
        }

        try{
            player.getSpaceship().getBuildingBoard().populateComponent("purple", x, y);
            sender.sendMessage("Purple alien placed at X: " + (x + 6 - gameManager.getGame().getLevel()) + " Y: " + (y + 5));

            askOrangeAlien(gameManager);
        } catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
            sender.sendMessage(new AskAlien("purple", player.getSpaceship()));
        }
    }

    private static void receiveOrangeAlien(GameManager gameManager, Player player, int x, int y) throws RemoteException {
        Game game = gameManager.getGame();

        Sender sender = gameManager.getSenderByPlayer(player);

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POPULATING))) {
            sender.sendMessage("IncorrectPhase");
        }

        if(x == -1 && y == -1){
            player.setIsReady(true, game);
            gameManager.getGameThread().notifyThread();
            return;
        }

        try{
            player.getSpaceship().getBuildingBoard().populateComponent("orange", x, y);
            sender.sendMessage("Orange alien placed at X: " + (x + 6 - gameManager.getGame().getLevel()) + " Y: " + (y + 5));

            player.setIsReady(true, game);
            gameManager.getGameThread().notifyThread();
        } catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
            sender.sendMessage(new AskAlien("orange", player.getSpaceship()));
        }
    }
}