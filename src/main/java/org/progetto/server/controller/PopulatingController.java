package org.progetto.server.controller;

import org.progetto.messages.toClient.Populating.AlienPlacedMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;


public class PopulatingController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * It asks all players if they want to place an alien on their spaceship
     *
     * @author Alessandro
     * @param gameManager the game manager
     */
    public static void askAliens(GameManager gameManager) {

        for (Player player : gameManager.getGame().getBoard().getCopyTravelers()){
            askAliensToSinglePlayer(gameManager, player);
        }
    }

    /**
     * Allows to ask only one player if he wants to place an alien on his spaceship
     *
     * @author Alessandro
     * @param gameManager is the current gameManager
     * @param player the player to ask for the aliens
     */
    public static void askAliensToSinglePlayer(GameManager gameManager, Player player) {

        Sender sender = gameManager.getSenderByPlayer(player);

        if(player.getIsReady()){
            MessageSenderService.sendMessage("PopulatingComplete", sender);
            return;
        }

        Spaceship spaceship = player.getSpaceship();

        if (!spaceship.getAlienPurple() && spaceship.checkShipAllowPurpleAlien())
            MessageSenderService.sendMessage(new AskAlienMessage("purple", spaceship), sender);

        else if (!spaceship.getAlienOrange() && spaceship.checkShipAllowOrangeAlien())
            MessageSenderService.sendMessage(new AskAlienMessage("orange", spaceship), sender);
    }

    /**
     * It receives the alien placed by the player
     *
     * @author Alessandro
     * @param gameManager the game manager
     * @param player the player who placed the alien
     * @param x the x coordinate of the alien
     * @param y the y coordinate of the alien
     * @param color the color of the alien
     */
    public static void receivePlaceAlien(GameManager gameManager, Player player, int x, int y, String color) {
        if(color.equals("purple"))
            receivePurpleAlien(gameManager, player, x, y);
        else
            receiveOrangeAlien(gameManager, player, x, y);
    }

    /**
     * It receives the purple alien placed by the player
     *
     * @author Alessandro
     * @param gameManager the game manager
     * @param player the player who placed the alien
     * @param x the x coordinate of the alien
     * @param y the y coordinate of the alien
     */
    private static void receivePurpleAlien(GameManager gameManager, Player player, int x, int y) {
        Sender sender = gameManager.getSenderByPlayer(player);

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)))
            MessageSenderService.sendMessage("IncorrectPhase", sender);

        if (x != -1 || y != -1) {
            try{
                player.getSpaceship().getBuildingBoard().placeAlienComponent("purple", x, y);
                MessageSenderService.sendMessage(new AlienPlacedMessage(x + 6 - gameManager.getGame().getLevel(), y + 5), sender);

            } catch (IllegalStateException e) {

                if(!e.getMessage().equals("PurpleAlienAlreadyPlaced")){
                    MessageSenderService.sendMessage(e.getMessage(), sender);
                    MessageSenderService.sendMessage(new AskAlienMessage("purple", player.getSpaceship()), sender);
                    return;
                }
            }
        }

        if (player.getSpaceship().checkShipAllowOrangeAlien())
            MessageSenderService.sendMessage(new AskAlienMessage("orange", player.getSpaceship()), sender);
        else {
            player.getSpaceship().getBuildingBoard().fillHumans();
            player.setIsReady(true);
            gameManager.getGameThread().notifyThread();

            MessageSenderService.sendMessage("PopulatingComplete", sender);
        }
    }

    /**
     * It receives the orange alien placed by the player
     *
     * @author Alessandro
     * @param gameManager the game manager
     * @param player the player who placed the alien
     * @param x the x coordinate of the alien
     * @param y the y coordinate of the alien
     */
    private static void receiveOrangeAlien(GameManager gameManager, Player player, int x, int y) {
        Game game = gameManager.getGame();

        Sender sender = gameManager.getSenderByPlayer(player);

        if (!(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)))
            MessageSenderService.sendMessage("IncorrectPhase", sender);

        if(x != -1 || y != -1){
            try{
                player.getSpaceship().getBuildingBoard().placeAlienComponent("orange", x, y);
                MessageSenderService.sendMessage(new AlienPlacedMessage(x + 6 - gameManager.getGame().getLevel(), y + 5), sender);

            } catch (IllegalStateException e) {

                if(!e.getMessage().equals("OrangeAlienAlreadyPlaced")){
                    MessageSenderService.sendMessage(e.getMessage(), sender);
                    MessageSenderService.sendMessage(new AskAlienMessage("orange", player.getSpaceship()), sender);
                    return;
                }
            }
        }

        player.getSpaceship().getBuildingBoard().fillHumans();
        player.setIsReady(true);
        gameManager.getGameThread().notifyThread();

        MessageSenderService.sendMessage("PopulatingComplete", sender);
    }

    /**
     * It fills with humans the spaceships of the disconnected players
     *
     * @author Alessandro
     * @param gameManager the game manager
     */
    public static void fillHumansDisconnectedPlayers(GameManager gameManager){
        for (Player player : gameManager.getDisconnectedPlayersCopy()){

            if(!player.getIsReady())
                player.getSpaceship().getBuildingBoard().fillHumans();
        }
    }
}