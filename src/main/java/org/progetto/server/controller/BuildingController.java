package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.EventCard;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Building phase controller class
 */
public class BuildingController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Handles player decision to pick a hidden component
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param sender
     */
    public static void pickHiddenComponent(GameManager gameManager, Player player, Sender sender) throws RemoteException{

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
            sender.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sender.sendMessage("FullHandComponent");

            if(e.getMessage().equals("EmptyComponentDeck"))
                sender.sendMessage("EmptyComponentDeck");
        }
    }

    /**
     * Handles player decision to pick a visible component
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param componentIdx
     * @param sender
     */
    public static void pickVisibleComponent(GameManager gameManager, Player player, int componentIdx, Sender sender) throws RemoteException{

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            gameManager.getGame().pickVisibleComponent(componentIdx, player);
            Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
            sender.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sender.sendMessage("FullHandComponent");

            if(e.getMessage().equals("IllegalIndexComponent"))
                sender.sendMessage("IllegalIndexComponent");
        }
    }

    /**
     * Place last component
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param sender
     */
    public static void placeLastComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);

        }else
            sender.sendMessage("NotAllowedToPlaceComponent");

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles player decision to pick hidden component, and place current hand component
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param sender
     */
    public static void placeHandComponentAndPickHiddenComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);
                Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
                sender.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");

                if (e.getMessage().equals("EmptyComponentDeck"))
                    sender.sendMessage("EmptyComponentDeck");
            }
        }else
            sender.sendMessage("NotAllowedToPlaceComponent");
    }

    /**
     * Handles player decision to pick visible component, and place current hand component
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param idxVisibleComponent
     * @param sender
     */
    public static void placeHandComponentAndPickVisibleComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);
                gameManager.getGame().pickVisibleComponent(idxVisibleComponent, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                sender.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");

                if (e.getMessage().equals("IllegalIndexComponent"))
                    sender.sendMessage("IllegalIndexComponent");
            }
        }else
            sender.sendMessage("NotAllowedToPlaceComponent");
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck, and place current hand component
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param deckIdx
     * @param sender
     */
    public static void placeHandComponentAndPickUpEventCardDeck(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);
                ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);
                sender.sendMessage(new PickedUpEventCardDeckMessage(eventCardsDeck));
                gameManager.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                    sender.sendMessage("EventCardDeckIsAlreadyTaken");
            }
        }else
            sender.sendMessage("NotAllowedToPlaceComponent");
    }

    /**
     * Handles player decision to pick a booked component, and place current hand component
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndPickBookedComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);

                gameManager.getGame().getPlayers().get(gameManager.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);
                String pickedComponentImg = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();
                //sender.sendMessage(new PickedComponentMessage(pickedComponentImg));
                sender.sendMessage("PickedBookedComponent");
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx, pickedComponentImg),sender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");
                else if (e.getMessage().equals("IllegalIndex"))
                    sender.sendMessage("IllegalIndex");
                else if (e.getMessage().equals("EmptyBookedCell"))
                    sender.sendMessage("EmptyBookedCell");
            }
        }else
            sender.sendMessage("NotAllowedToPlaceComponent");
    }

    /**
     * Handles the player decision to discard its hand component
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param sender
     */
    public static void discardComponent(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            String imgSrc = gameManager.getGame().discardComponent(player);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiscardComponentMessage(player.getName(), imgSrc), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to book a component
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param idx where the booked component will be inserted
     * @param sender
     */
    public static void bookComponent(GameManager gameManager, Player player, int idx, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try {
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

            gameManager.getGame().getPlayers().get(gameManager.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().setAsBooked(idx);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBookedComponentMessage(player.getName(),imgSrc,idx),sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else if (e.getMessage().equals("IllegalIndex"))
                sender.sendMessage("IllegalIndex");
            else if (e.getMessage().equals("BookedCellOccupied"))
                sender.sendMessage("BookedCellOccupied");
        }
    }

    /**
     * Handles player decision to pick a booked component
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param player that want to pick a booked component
     * @param idx of the component to pick
     * @param sender
     */
    public static void pickBookedComponent(GameManager gameManager, Player player, int idx, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            gameManager.getGame().getPlayers().get(gameManager.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();
            //sender.sendMessage(new PickedComponentMessage(imgSrc));
            sender.sendMessage("PickedBookedComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx, imgSrc),sender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sender.sendMessage("FullHandComponent");
            else if (e.getMessage().equals("IllegalIndex"))
                sender.sendMessage("IllegalIndex");
            else if (e.getMessage().equals("EmptyBookedCell"))
                sender.sendMessage("EmptyBookedCell");
        }
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param deckIdx
     * @param sender
     */
    public static void pickUpEventCardDeck(GameManager gameManager, Player player, int deckIdx, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);
            sender.sendMessage(new PickedUpEventCardDeckMessage(eventCardsDeck));
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                sender.sendMessage("EventCardDeckIsAlreadyTaken");
            if(e.getMessage().equals("IllegalIndexEventCardDeck"))
                sender.sendMessage("IllegalIndexEventCardDeck");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to put-down a current eventCard deck
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param sender
     */
    public static void putDownEventCardDeck(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            int deckIdx = gameManager.getGame().putDownEventCardDeck(player);
            sender.sendMessage("EventCardDeckPutDown");
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerPutDownEventCardDeckMessage(player.getName(), deckIdx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NoEventCardDeckTaken"))
                sender.sendMessage("NoEventCardDeckTaken");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles the destruction of a component
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param player owner of the spaceship
     * @param yComponent coordinate
     * @param xComponent coordinate
     * @param sender
     */
    public static void destroyComponent(GameManager gameManager, Player player, int yComponent, int xComponent, Sender sender) throws RemoteException {

        try{
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
            buildingBoard.destroyComponent(yComponent,xComponent);

            sender.sendMessage(new DestroyedComponentMessage(yComponent,xComponent));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), yComponent, xComponent), sender);

        } catch (IllegalStateException e) {
            if (e.getMessage().equals("EmptyComponentCell"))
                sender.sendMessage("EmptyComponentCell");
        }
    }

    /**
     * Defines a player ready for the game
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param sender
     */
    public static void readyBuilding(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGame().getBoard().addReadyTraveler(player);
            sender.sendMessage("YouAreReady");
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerIsReadyMessage(player.getName()), sender);
            gameManager.getGameThread().notifyThread();

        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerIsAlreadyReady"))
                sender.sendMessage("PlayerIsAlreadyReady");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Resets timer
     *
     * @author Alessandro
     * @param gameManager
     * @param sender
     */
    public static void resetTimer(GameManager gameManager, Sender sender) throws RemoteException{
        try {
            gameManager.getTimerController().resetTimer();
        }catch (IllegalStateException e){
            if(e.getMessage().equals("ImpossibleToResetTimer"))
                sender.sendMessage("ImpossibleToResetTimer");
        }
    }

    /**
     * Checks the validity of the spaceship for each player
     * todo: rimuovere dai ready travelers?
     *
     * @author Alessandro
     * @param gameManager
     */
    public static void checkAllShipValidity(GameManager gameManager) {
        Game game = gameManager.getGame();

        for (Player player : game.getPlayers()) {

            if(!player.getSpaceship().getBuildingBoard().checkStartShipValidity()){
                player.setIsReady(false, game);

                Sender sender = gameManager.getSocketWriterByPlayer(player);

                if(sender == null)
                    sender = gameManager.getVirtualClientByPlayer(player);

                try {
                    sender.sendMessage("NotValidSpaceShip");
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
