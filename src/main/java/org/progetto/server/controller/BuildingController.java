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

    // todo: add check if is building phase and check if the player is already ready

    /**
     * Handles player decision to pick a hidden component
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param sender
     */
    public static void pickHiddenComponent(GameManager gameManager, Player player, Sender sender) throws RemoteException{

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
            sender.sendMessage(new PickedComponentMessage(pickedComponent));

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sender.sendMessage("FullHandComponent");

            if(e.getMessage().equals("EmptyComponentDeck"))
                sender.sendMessage("EmptyComponentDeck");

            System.out.println(e.getMessage());
        }
    }

    /**
     * Handles player decision to show visible components
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public static void showVisibleComponents(GameManager gameManager, Player player, Sender sender) throws RemoteException {
        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try {
            ArrayList<Component> visibleDeck = gameManager.getGame().getVisibleComponentDeckCopy();
            sender.sendMessage(new ShowVisibleComponentsMessage(visibleDeck));

        } catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
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

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            gameManager.getGame().pickVisibleComponent(componentIdx, player);
            Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
            sender.sendMessage(new PickedComponentMessage(pickedComponent));

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
     * @param xPlaceComponent
     * @param yPlaceComponent
     * @param rPlaceComponent
     * @param sender
     */
    public static void placeLastComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        if(buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), buildingBoard.getCopySpaceshipMatrix()[yPlaceComponent][xPlaceComponent]), sender);

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
     * @param xPlaceComponent
     * @param yPlaceComponent
     * @param rPlaceComponent
     * @param sender
     */
    public static void placeHandComponentAndPickHiddenComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        if(buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), buildingBoard.getCopySpaceshipMatrix()[yPlaceComponent][xPlaceComponent]), sender);
                Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
                sender.sendMessage(new PickedComponentMessage(pickedComponent));

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
     * @param xPlaceComponent
     * @param yPlaceComponent
     * @param rPlaceComponent
     * @param idxVisibleComponent
     * @param sender
     */
    public static void placeHandComponentAndPickVisibleComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent, Sender sender) throws RemoteException {

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();


        if(buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), buildingBoard.getCopySpaceshipMatrix()[yPlaceComponent][xPlaceComponent]), sender);
                gameManager.getGame().pickVisibleComponent(idxVisibleComponent, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                sender.sendMessage(new PickedComponentMessage(pickedComponent));

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
     * @param xPlaceComponent
     * @param yPlaceComponent
     * @param rPlaceComponent
     * @param deckIdx
     * @param sender
     */
    public static void placeHandComponentAndPickUpEventCardDeck(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx, Sender sender) throws RemoteException {

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();


        if(buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), buildingBoard.getCopySpaceshipMatrix()[yPlaceComponent][xPlaceComponent]), sender);
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
     * @param xPlaceComponent
     * @param yPlaceComponent
     * @param rPlaceComponent
     * @param idx
     * @param sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndPickBookedComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx, Sender sender) throws RemoteException {

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        if(buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), buildingBoard.getCopySpaceshipMatrix()[yPlaceComponent][xPlaceComponent]), sender);

                gameManager.getGame().getPlayersCopy().get(gameManager.getGame().getPlayersCopy().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);
                String pickedComponentImg = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

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
     * Try to place component and set player as ready
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param xPlaceComponent
     * @param yPlaceComponent
     * @param rPlaceComponent
     * @param sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndReady(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();


        if(buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent)){
            sender.sendMessage("AllowedToPlaceComponent");

            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), buildingBoard.getCopySpaceshipMatrix()[yPlaceComponent][xPlaceComponent]), sender);
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGame().getBoard().addTraveler(player);
            sender.sendMessage("YouAreReady");
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerIsReadyMessage(player.getName()), sender);
            gameManager.getGameThread().notifyThread();

        }else
            sender.sendMessage("NotAllowedToPlaceComponent");
    }

    /**
     * Set player as ready in building
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public static void readyBuilding(GameManager gameManager, Player player, Sender sender) throws RemoteException{
        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        sender.sendMessage("YouAreReady");
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerIsReadyMessage(player.getName()), sender);
        gameManager.getGame().getBoard().addTraveler(player);
        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
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

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            String imgSrc = gameManager.getGame().discardComponent(player);
            sender.sendMessage("HandComponentDiscarded");
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

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try {
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();
            player.getSpaceship().getBuildingBoard().setAsBooked(idx);
            sender.sendMessage("ComponentBooked");
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
     * Handles player decision to show booked components
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param sender
     * @throws RemoteException
     */
    public static void showBookedComponents(GameManager gameManager, Player player, Sender sender) throws RemoteException {
        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try {
            Component[] bookedComponents = player.getSpaceship().getBuildingBoard().getBookedCopy();
            sender.sendMessage(new ShowBookedComponentsMessage(bookedComponents));

        } catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
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

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            player.getSpaceship().getBuildingBoard().pickBookedComponent(idx);
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

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

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

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

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

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
     * @return areAllValid
     */
    public static boolean checkAllShipValidity(GameManager gameManager) {
        Game game = gameManager.getGame();
        boolean areAllValid = true;

        for (Player player : game.getPlayersCopy()) {

            if(!player.getSpaceship().getBuildingBoard().checkStartShipValidity()){
                areAllValid = false;
                player.setIsReady(false, game);
                game.getBoard().removeTraveler(player);

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

        return areAllValid;
    }
}
