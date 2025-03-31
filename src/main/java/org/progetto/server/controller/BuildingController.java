package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.EventCard;

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
     * @param swSender
     * @param vvSender
     */
    public static void pickHiddenComponent(GameCommunicationHandler gameCommunicationHandler, Player player, SocketWriter swSender, VirtualClient vvSender){

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            Component pickedComponent = gameCommunicationHandler.getGame().pickHiddenComponent(player);
            GameController.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                GameController.sendMessage("FullHandComponent", swSender, vvSender);

            if(e.getMessage().equals("EmptyComponentDeck"))
                GameController.sendMessage("EmptyComponentDeck", swSender, vvSender);
        }
    }

    /**
     * Handles player decision to pick a visible component
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param componentIdx
     * @param swSender
     * @param vvSender
     */
    public static void pickVisibleComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int componentIdx, SocketWriter swSender, VirtualClient vvSender){

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            gameCommunicationHandler.getGame().pickVisibleComponent(componentIdx, player);
            Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
            GameController.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                GameController.sendMessage("FullHandComponent", swSender, vvSender);

            if(e.getMessage().equals("IllegalIndexComponent"))
                GameController.sendMessage("IllegalIndexComponent", swSender, vvSender);
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
     * @param swSender
     * @param vvSender
     */
    public static void placeHandComponentAndPickHiddenComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), swSender, vvSender);
                Component pickedComponent = gameCommunicationHandler.getGame().pickHiddenComponent(player);
                GameController.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    GameController.sendMessage("FullHandComponent", swSender, vvSender);

                if (e.getMessage().equals("EmptyComponentDeck"))
                    GameController.sendMessage("EmptyComponentDeck", swSender, vvSender);
            }
        }else
            GameController.sendMessage("ImpossiblePlaceComponent", swSender, vvSender);
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
     * @param swSender
     * @param vvSender
     */
    public static void placeHandComponentAndPickVisibleComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), swSender, vvSender);
                gameCommunicationHandler.getGame().pickVisibleComponent(componentIdx, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                GameController.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    GameController.sendMessage("FullHandComponent", swSender, vvSender);

                if (e.getMessage().equals("IllegalIndexComponent"))
                    GameController.sendMessage("IllegalIndexComponent", swSender, vvSender);
            }
        }else
            GameController.sendMessage("ImpossiblePlaceComponent", swSender, vvSender);
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck, and place current hand component
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param yPlaceComponent
     * @param xPlaceComponent
     * @param rPlaceComponent
     * @param deckIdx
     * @param swSender
     * @param vvSender
     */
    public static void placeHandComponentAndPickUpEventCardDeck(GameCommunicationHandler gameCommunicationHandler, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), swSender, vvSender);
                ArrayList<EventCard> eventCardsDeck = gameCommunicationHandler.getGame().pickUpEventCardDeck(player, deckIdx);
                GameController.sendMessage(new PickedUpEventCardDeckMessage(eventCardsDeck), swSender, vvSender);
                gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), swSender, vvSender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                    GameController.sendMessage("EventCardDeckIsAlreadyTaken", swSender, vvSender);
            }
        }else
            GameController.sendMessage("ImpossiblePlaceComponent", swSender, vvSender);
    }

    /**
     * Handles the player decision to discard its hand component
     *
     * @author Lorenzo
     * @param gameCommunicationHandler is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param swSender sender for socket
     * @param vvSender registry for RMI
     */
    public static void discardComponent(GameCommunicationHandler gameCommunicationHandler, Player player, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            String imgSrc = gameCommunicationHandler.getGame().discardComponent(player);
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerDiscardComponentMessage(player.getName(), imgSrc), swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                GameController.sendMessage("EmptyHandComponent", swSender, vvSender);
            else
                GameController.sendMessage(e.getMessage(), swSender, vvSender);
        }
    }

    /**
     * Handles the player decision to book a component
     *
     * @author Lorenzo
     * @param gameCommunicationHandler is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param idx where the booked component will be inserted
     * @param swSender sender for socket
     * @param vvSender registry for RMI
     */
    public static void bookComponent(GameCommunicationHandler gameCommunicationHandler, Player player, int idx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try {
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

            gameCommunicationHandler.getGame().getPlayers().get(gameCommunicationHandler.getGame().getPlayers().indexOf(player)).getSpaceship().getBuildingBoard().setAsBooked(idx);
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerBookedComponentMessage(player.getName(),imgSrc,idx),swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                GameController.sendMessage("EmptyHandComponent", swSender, vvSender);
            else if (e.getMessage().equals("IllegalIndex"))
                GameController.sendMessage("IllegalIndex", swSender, vvSender);
            else if (e.getMessage().equals("BookedCellOccupied"))
                GameController.sendMessage("BookedCellOccupied", swSender, vvSender);
        }
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param deckIdx
     * @param swSender
     * @param vvSender
     */
    public static void pickUpEventCardDeck(GameCommunicationHandler gameCommunicationHandler, Player player, int deckIdx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            ArrayList<EventCard> eventCardsDeck = gameCommunicationHandler.getGame().pickUpEventCardDeck(player, deckIdx);
            GameController.sendMessage(new PickedUpEventCardDeckMessage(eventCardsDeck), swSender, vvSender);
            gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                GameController.sendMessage("EventCardDeckIsAlreadyTaken", swSender, vvSender);
            if(e.getMessage().equals("IllegalIndexEventCardDeck"))
                GameController.sendMessage("IllegalIndexEventCardDeck", swSender, vvSender);
            else
                GameController.sendMessage(e.getMessage(), swSender, vvSender);
        }
    }

    /**
     * Handles player decision to put-down a current eventCard deck
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param swSender
     * @param vvSender
     */
    public static void putDownEventCardDeck(GameCommunicationHandler gameCommunicationHandler, Player player, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            int deckIdx = gameCommunicationHandler.getGame().putDownEventCardDeck(player);
            GameController.sendMessage("EventCardDeckPutDown", swSender, vvSender);
            gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerPutDownEventCardDeckMessage(player.getName(), deckIdx), swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NoEventCardDeckTaken"))
                GameController.sendMessage("NoEventCardDeckTaken", swSender, vvSender);
            else
                GameController.sendMessage(e.getMessage(), swSender, vvSender);
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
     * @param swSender sender for socket
     * @param vvSender registry for RMI
     */
    public static void destroyComponent(GameCommunicationHandler gameCommunicationHandler, Player player,int yComponent, int xComponent, SocketWriter swSender ,VirtualClient vvSender) {

        try{
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
            String imgSrc = buildingBoard.getHandComponent().getImgSrc();
            buildingBoard.destroyComponent(yComponent,xComponent);

            GameController.sendMessage("ComponentDestroyed", swSender, vvSender);  //forse da aggiungere un messaggio con parametri
            gameCommunicationHandler.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponent(player,yComponent,xComponent), swSender, vvSender);

        } catch (IllegalStateException e) {
            if (e.getMessage().equals("EmptyComponentCell"))
                GameController.sendMessage("EmptyComponentCell", swSender, vvSender);
        }
    }

    /**
     * Defines a player ready for the game
     *
     * @author Gabriele
     * @param gameCommunicationHandler
     * @param player
     * @param swSender
     * @param vvSender
     */
    public static void playerReady(GameCommunicationHandler gameCommunicationHandler, Player player, SocketWriter swSender, VirtualClient vvSender) {

        if(gameCommunicationHandler.timerExpired()){
            GameController.sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            gameCommunicationHandler.getGame().getBoard().addReadyTraveler(player);
            GameController.sendMessage("YouAreReady", swSender, vvSender);
            gameCommunicationHandler.broadcastGameMessageToOthers( new AnotherPlayerIsReadyMessage(player.getName()), swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerIsAlreadyReady"))
                GameController.sendMessage("PlayerIsAlreadyReady", swSender, vvSender);
            else
                GameController.sendMessage(e.getMessage(), swSender, vvSender);
        }
    }


    public static void resetTimer(GameCommunicationHandler gameCommunicationHandler, SocketWriter swSender, VirtualClient vvSender){
        try {
            gameCommunicationHandler.getTimerController().resetTimer();
        }catch (IllegalStateException e){
            if(e.getMessage().equals("ImpossibleToResetTimer"))
                GameController.sendMessage("ImpossibleToResetTimer", swSender, vvSender);
        }
    }
}
