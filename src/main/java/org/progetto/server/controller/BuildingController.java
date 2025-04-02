package org.progetto.server.controller;

import org.progetto.messages.toClient.*;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameCommunicationHandler;
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
     * @param gameCommunicationHandler
     * @param player
     * @param sender
     */
    public static void pickHiddenComponent(GameCommunicationHandler gameCommunicationHandler, Player player, Sender sender) throws RemoteException{

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            Component pickedComponent = gameCommunicationHandler.getGame().pickHiddenComponent(player);
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
     * @param gameCommunicationHandler
     * @param player
     * @param componentIdx
     * @param sender
     */
    public static void pickVisibleComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int componentIdx, Sender sender) throws RemoteException{

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            gameCommunicationHandler.getGame().pickVisibleComponent(componentIdx, player);
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
     * Handles player decision to pick hidden component, and place current hand component
     *
     * @author Alessandro
     * @param gameCommunicationHandler
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param sender
     */
    public static void placeHandComponentAndPickHiddenComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);
                Component pickedComponent = gameCommunicationHandler.getGame().pickHiddenComponent(player);
                sender.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");

                if (e.getMessage().equals("EmptyComponentDeck"))
                    sender.sendMessage("EmptyComponentDeck");
            }
        }else
            sender.sendMessage("ImpossiblePlaceComponent");
    }

    /**
     * Handles player decision to pick visible component, and place current hand component
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param componentIdx
     * @param sender
     */
    public static void placeHandComponentAndPickVisibleComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);
                gameCommunicationHandler.getGame().pickVisibleComponent(componentIdx, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                sender.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");

                if (e.getMessage().equals("IllegalIndexComponent"))
                    sender.sendMessage("IllegalIndexComponent");
            }
        }else
            sender.sendMessage("ImpossiblePlaceComponent");
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck, and place current hand component
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param deckIdx
     * @param sender
     */
    public static void placeHandComponentAndPickUpEventCardDeck(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);
                ArrayList<EventCard> eventCardsDeck = gameCommunicationHandler.getGame().pickUpEventCardDeck(player, deckIdx);
                sender.sendMessage(new PickedUpEventCardDeckMessage(eventCardsDeck));
                gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                    sender.sendMessage("EventCardDeckIsAlreadyTaken");
            }
        }else
            sender.sendMessage("ImpossiblePlaceComponent");
    }

    /**
     * Handles player decision to pick a booked component, and place current hand component
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndPickBookedComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int idx, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), sender);

                gameCommunicationHandler.getGame().getPlayers().get(gameCommunicationHandler.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);
                String pickedComponentImg = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();
                //sender.sendMessage(new PickedComponentMessage(pickedComponentImg));
                sender.sendMessage("PickedBookedComponent");
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx, pickedComponentImg),sender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");
                else if (e.getMessage().equals("IllegalIndex"))
                    sender.sendMessage("IllegalIndex");
                else if (e.getMessage().equals("EmptyBookedCell"))
                    sender.sendMessage("EmptyBookedCell");
            }
        }else
            sender.sendMessage("ImpossiblePlaceComponent");
    }

    /**
     * Handles the player decision to discard its hand component
     *
     * @author Lorenzo
     * @param gameCommunicationHandler is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param sender
     */
    public static void discardComponent(GameCommunicationHandler gameCommunicationHandler, Player player, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            String imgSrc = gameCommunicationHandler.getGame().discardComponent(player);
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerDiscardComponentMessage(player.getName(), imgSrc), sender);

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
     * @param gameCommunicationHandler is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param idx where the booked component will be inserted
     * @param sender
     */
    public static void bookComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int idx, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try {
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

            gameCommunicationHandler.getGame().getPlayers().get(gameCommunicationHandler.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().setAsBooked(idx);
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerBookedComponentMessage(player.getName(),imgSrc,idx),sender);

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
     * @param gameCommunicationHandler is the class that manage the current game
     * @param player that want to pick a booked component
     * @param idx of the component to pick
     * @param sender
     */
    public static void pickBookedComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int idx, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            gameCommunicationHandler.getGame().getPlayers().get(gameCommunicationHandler.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();
            //sender.sendMessage(new PickedComponentMessage(imgSrc));
            sender.sendMessage("PickedBookedComponent");
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx, imgSrc),sender);

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
     * @param gameCommunicationHandler
     * @param player
     * @param deckIdx
     * @param sender
     */
    public static void pickUpEventCardDeck(GameCommunicationHandler gameCommunicationHandler, Player player, int deckIdx, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            ArrayList<EventCard> eventCardsDeck = gameCommunicationHandler.getGame().pickUpEventCardDeck(player, deckIdx);
            sender.sendMessage(new PickedUpEventCardDeckMessage(eventCardsDeck));
            gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

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
     * @param gameCommunicationHandler
     * @param player
     * @param sender
     */
    public static void putDownEventCardDeck(GameCommunicationHandler gameCommunicationHandler, Player player, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            int deckIdx = gameCommunicationHandler.getGame().putDownEventCardDeck(player);
            sender.sendMessage("EventCardDeckPutDown");
            gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerPutDownEventCardDeckMessage(player.getName(), deckIdx), sender);

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
     * @param gameCommunicationHandler is the class that manage the current game
     * @param player owner of the spaceship
     * @param yComponent coordinate
     * @param xComponent coordinate
     * @param sender
     */
    public static void destroyComponent(GameCommunicationHandler gameCommunicationHandler, Player player,int yComponent, int xComponent, Sender sender) throws RemoteException {

        try{
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
            String imgSrc = buildingBoard.getHandComponent().getImgSrc();
            buildingBoard.destroyComponent(yComponent,xComponent);

            sender.sendMessage("ComponentDestroyed");  //forse da aggiungere un messaggio con parametri
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponent(player,yComponent,xComponent), sender);

        } catch (IllegalStateException e) {
            if (e.getMessage().equals("EmptyComponentCell"))
                sender.sendMessage("EmptyComponentCell");
        }
    }

    /**
     * Defines a player ready for the game
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param sender
     */
    public static void playerReady(GameCommunicationHandler gameCommunicationHandler, Player player, Sender sender) throws RemoteException {

        if(gameCommunicationHandler.timerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{
            gameCommunicationHandler.getGame().getBoard().addReadyTraveler(player);
            sender.sendMessage("YouAreReady");
            gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerIsReadyMessage(player.getName()), sender);

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
     * @param gameCommunicationHandler
     * @param sender
     */
    public static void resetTimer(GameCommunicationHandler gameCommunicationHandler, Sender sender) throws RemoteException{
        try {
            gameCommunicationHandler.getTimerController().resetTimer();
        }catch (IllegalStateException e){
            if(e.getMessage().equals("ImpossibleToResetTimer"))
                sender.sendMessage("ImpossibleToResetTimer");
        }
    }

    /**
     * Checks the validity of the spaceship for each player
     *
     * @author Alessandro
     * @param gameCommunicationHandler
     */
    public static void checkShipValidity(GameCommunicationHandler gameCommunicationHandler) {
        Game game = gameCommunicationHandler.getGame();

        for (Player player : game.getPlayers()) {

            if(!player.getSpaceship().getBuildingBoard().checkShipValidity()){
                game.addReadyPlayers(false);

                Sender sender = gameCommunicationHandler.getSocketWriterByPlayer(player);

                if(sender == null)
                    sender = gameCommunicationHandler.getVirtualClientByPlayer(player);

                try {
                    sender.sendMessage("NotValidSpaceShip");
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
