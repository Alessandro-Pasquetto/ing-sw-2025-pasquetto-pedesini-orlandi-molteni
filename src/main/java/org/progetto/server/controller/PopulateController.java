package org.progetto.server.controller;

import org.progetto.messages.toClient.Populating.AskAlienMessage;
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

        for (Player player : gameManager.getGame().getBoard().getCopyTravelers()){
            if (!player.getIsReady() && player.getSpaceship().checkShipAllowPurpleAlien()){

                Sender sender = gameManager.getSenderByPlayer(player);
                sender.sendMessage(new AskAlienMessage("purple", player.getSpaceship()));
            }

            else if (!player.getIsReady() && player.getSpaceship().checkShipAllowOrangeAlien()){

                Sender sender = gameManager.getSenderByPlayer(player);
                sender.sendMessage(new AskAlienMessage("orange", player.getSpaceship()));
            }

            else {
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
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

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)))
            sender.sendMessage("IncorrectPhase");

        if(x != -1 || y != -1){
            try{
                player.getSpaceship().getBuildingBoard().placeAlienComponent("purple", x, y);
                sender.sendMessage("Purple alien placed at X: " + (x + 6 - gameManager.getGame().getLevel()) + " Y: " + (y + 5));

            } catch (IllegalStateException e) {
                sender.sendMessage(e.getMessage());
                sender.sendMessage(new AskAlienMessage("purple", player.getSpaceship()));
                return;
            }
        }

        if (player.getSpaceship().checkShipAllowOrangeAlien())
            sender.sendMessage(new AskAlienMessage("orange", player.getSpaceship()));
        else{
            player.getSpaceship().getBuildingBoard().fillHumans();
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();
        }
    }

    private static void receiveOrangeAlien(GameManager gameManager, Player player, int x, int y) throws RemoteException {
        Game game = gameManager.getGame();

        Sender sender = gameManager.getSenderByPlayer(player);

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)))
            sender.sendMessage("IncorrectPhase");

        if(x != -1 || y != -1){
            try{
                player.getSpaceship().getBuildingBoard().placeAlienComponent("orange", x, y);
                sender.sendMessage("Orange alien placed at X: " + (x + 6 - gameManager.getGame().getLevel()) + " Y: " + (y + 5));

            } catch (IllegalStateException e) {
                sender.sendMessage(e.getMessage());
                sender.sendMessage(new AskAlienMessage("orange", player.getSpaceship()));
                return;
            }
        }

        player.getSpaceship().getBuildingBoard().fillHumans();
        player.setIsReady(true, game);
        gameManager.getGameThread().notifyThread();
    }

    public static void fillHumansDisconnectedPlayers(GameManager gameManager){

        for (Player player : gameManager.getDisconnectedPlayersCopy()){

            if(!player.getIsReady())
                player.getSpaceship().getBuildingBoard().fillHumans();
        }

    }
}