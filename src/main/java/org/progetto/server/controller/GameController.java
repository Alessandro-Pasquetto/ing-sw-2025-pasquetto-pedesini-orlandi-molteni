package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.EventCard;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final AtomicInteger currentIdGame = new AtomicInteger(0);

    // =======================
    // OTHER METHODS
    // =======================

    private static void sendMessage(Object message, SocketWriter swSender, VirtualClient vvSender){
        if(swSender != null)
            swSender.sendMessage(message);
        else {
            try {
                vvSender.sendMessage(message);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void startGame(GameManager gameManager){
        gameManager.broadcastGameMessage("StartGame");

        gameManager.getGame().setPhase(GamePhase.BUILDING);

        gameManager.startTimer();
    }

    /**
     * Handles player decision to pick a hidden component
     *
     * @author Alessandro
     * @param gameManager
     * @param player
     * @param swSender
     * @param vvSender
     */
    public static void pickHiddenComponent(GameManager gameManager, Player player, SocketWriter swSender, VirtualClient vvSender){

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
            sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sendMessage("FullHandComponent", swSender, vvSender);

            if(e.getMessage().equals("EmptyComponentDeck"))
                sendMessage("EmptyComponentDeck", swSender, vvSender);
        }
    }

    /**
     * Handles player decision to pick a visible component
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param componentIdx
     * @param swSender
     * @param vvSender
     */
    public static void pickVisibleComponent(GameManager gameManager, Player player, int componentIdx, SocketWriter swSender, VirtualClient vvSender){

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            gameManager.getGame().pickVisibleComponent(componentIdx, player);
            Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
            sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sendMessage("FullHandComponent", swSender, vvSender);

            if(e.getMessage().equals("IllegalIndexComponent"))
                sendMessage("IllegalIndexComponent", swSender, vvSender);
        }
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
     * @param swSender
     * @param vvSender
     */
    public static void placeHandComponentAndPickHiddenComponent(GameManager gameManager, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, SocketWriter swSender, VirtualClient vvSender) {

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), swSender, vvSender);
                Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
                sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sendMessage("FullHandComponent", swSender, vvSender);

                if (e.getMessage().equals("EmptyComponentDeck"))
                    sendMessage("EmptyComponentDeck", swSender, vvSender);
            }
        }else
            sendMessage("ImpossiblePlaceComponent", swSender, vvSender);
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
     * @param componentIdx
     * @param swSender
     * @param vvSender
     */
    public static void placeHandComponentAndPickVisibleComponent(GameManager gameManager, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), swSender, vvSender);
                gameManager.getGame().pickVisibleComponent(componentIdx, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sendMessage("FullHandComponent", swSender, vvSender);

                if (e.getMessage().equals("IllegalIndexComponent"))
                    sendMessage("IllegalIndexComponent", swSender, vvSender);
            }
        }else
            sendMessage("ImpossiblePlaceComponent", swSender, vvSender);
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
     * @param swSender
     * @param vvSender
     */
    public static void placeHandComponentAndPickUpEventCardDeck(GameManager gameManager, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        String imgSrc = buildingBoard.getHandComponent().getImgSrc();
        if(buildingBoard.placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc), swSender, vvSender);
                ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);
                sendMessage(new PickedUpEventCardDeck(eventCardsDeck), swSender, vvSender);
                gameManager.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), swSender, vvSender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                    sendMessage("EventCardDeckIsAlreadyTaken", swSender, vvSender);
            }
        }else
            sendMessage("ImpossiblePlaceComponent", swSender, vvSender);
    }

    /**
     * Handles the player decision to discard its hand component
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param swSender sender for socket
     * @param vvSender registry for RMI
     */
    public static void discardComponent(GameManager gameManager, Player player, SocketWriter swSender, VirtualClient vvSender) {

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            String imgSrc = gameManager.getGame().discardComponent(player);
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerDiscardComponentMessage(player.getName(), imgSrc), swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                sendMessage("EmptyHandComponent", swSender, vvSender);
            else
                sendMessage(e.getMessage(), swSender, vvSender);
        }
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param deckIdx
     * @param swSender
     * @param vvSender
     */
    public static void pickUpEventCardDeck(GameManager gameManager, Player player, int deckIdx, SocketWriter swSender, VirtualClient vvSender) {

        if(gameManager.timerExpired()){
            sendMessage("TimerExpired", swSender, vvSender);
            return;
        }

        try{
            ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);
            sendMessage(new PickedUpEventCardDeck(eventCardsDeck), swSender, vvSender);
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), swSender, vvSender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                sendMessage("EventCardDeckIsAlreadyTaken", swSender, vvSender);
            else
                sendMessage(e.getMessage(), swSender, vvSender);
        }
    }
}