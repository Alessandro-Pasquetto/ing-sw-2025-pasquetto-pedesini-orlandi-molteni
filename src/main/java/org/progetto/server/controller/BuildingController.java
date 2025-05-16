package org.progetto.server.controller;

import javafx.util.Pair;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
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
     * Handles player decision to show hand component
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showHandComponent(GameManager gameManager, Player player, Sender sender) throws RemoteException{

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if (player.getIsReady()) {
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if (gameManager.getTimerExpired()) {
            sender.sendMessage("TimerExpired");
            return;
        }

        try {
            Component handComponent = player.getSpaceship().getBuildingBoard().getHandComponent();

            if (handComponent != null) {
                sender.sendMessage(new ShowHandComponentMessage(handComponent));
            } else {
                sender.sendMessage("EmptyHandComponent");
            }

        } catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to pick a hidden component
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void pickHiddenComponent(GameManager gameManager, Player player, Sender sender) throws RemoteException{

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if(player.equals(playerCheck)) {
                sender.sendMessage("FullHandEventDeck");
                return;
            }
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
     * Debug method used to build a default spaceship
     *
     * @param gameManager is the current gameManager
     * @param player that required the spaceship to be build
     * @param idShip type of spaceship to be build
     * @param sender
     * @throws RemoteException
     */
    public static void buildShip(GameManager gameManager, Player player, int idShip, Sender sender) throws RemoteException {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        try{

            int spaceshipLevel = player.getSpaceship().getLevelShip();
            BuildingBoard bb = player.getSpaceship().getBuildingBoard();

            // Level 1 ships
            if (spaceshipLevel == 1) {

                switch (idShip) {
                    case 1:
                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(2, 0, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 2, 3);

                        bb.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(0, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(2, 3, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(1, 3, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(0, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(0, 4, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(1, 4, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(3, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(3, 3, 2);

                        bb.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(4, 3, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(3, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(4, 4, 0);

                        sender.sendMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    case 2:
                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(2, 0, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(1, 2, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(1, 3, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(3, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(3, 3, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(0, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(2, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(0, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(4, 3, 0);

                        sender.sendMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    default:
                        sender.sendMessage("IDShipOutOfBounds");
                }

            // Level 2 ships
            } else if (spaceshipLevel == 2) {

                switch (idShip) {
                    case 1:
                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(4, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(5, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(2, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(1, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(5, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(5, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(2, 3, 2);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(3, 3, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(4, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(2, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(4, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(1, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(5, 4, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(0, 2, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(6, 2, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(0, 3, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 1));
                        bb.placeComponent(6, 3, 0);

                        sender.sendMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    case 2:
                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(4, 1, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(2, 0, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(2, 2, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
                        bb.placeComponent(1, 2, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(3, 3, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(2, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(1, 3, 1);

                        bb.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(0, 3, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(0, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(4, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(4, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(5, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
                        bb.placeComponent(5, 3, 1);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
                        bb.placeComponent(5, 4, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 1));
                        bb.placeComponent(6, 2, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
                        bb.placeComponent(6, 4, 0);

                        sender.sendMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    default:
                        sender.sendMessage("IDShipOutOfBounds");
                }

            }

        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Handles player decision to show visible components
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showVisibleComponents(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
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
     * @param gameManager current gameManager
     * @param player current player
     * @param componentIdx idx of the visible component's array
     * @param sender current sender
     * @throws RemoteException
     */
    public static void pickVisibleComponent(GameManager gameManager, Player player, int componentIdx, Sender sender) throws RemoteException{

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if (player.equals(playerCheck)) {
                sender.sendMessage("FullHandEventDeck");
                return;
            }
        }

        try{
            gameManager.getGame().pickVisibleComponent(componentIdx, player);
            Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
            sender.sendMessage(new PickedComponentMessage(pickedComponent));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedVisibleComponentMessage(player.getName(), pickedComponent), sender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sender.sendMessage("FullHandComponent");

            if(e.getMessage().equals("IllegalIndexComponent"))
                sender.sendMessage("IllegalIndexComponent");
        }
    }

    /**
     * Place component
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param sender current sender
     * @throws RemoteException
     */
    public static void placeComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if (player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if (gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            sender.sendMessage("AllowedToPlaceComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");
            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Place last component
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param sender current sender
     * @throws RemoteException
     */
    public static void placeLastComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            sender.sendMessage("AllowedToPlaceComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");
            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }

        player.setIsReady(true, gameManager.getGame());
        gameManager.addNotCheckedReadyPlayer(player);
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles player decision to pick hidden component, and place current hand component
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param sender current sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndPickHiddenComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);
                Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
                sender.sendMessage(new PickedComponentMessage(pickedComponent));

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");

                if (e.getMessage().equals("EmptyComponentDeck"))
                    sender.sendMessage("EmptyComponentDeck");
            }
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");
            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to pick visible component, and place current hand component
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param idxVisibleComponent idx of visible component's array
     * @param sender current sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndPickVisibleComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{

            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            sender.sendMessage("AllowedToPlaceComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);
            try{
                gameManager.getGame().pickVisibleComponent(idxVisibleComponent, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                sender.sendMessage(new PickedComponentMessage(pickedComponent));
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedVisibleComponentMessage(player.getName(), pickedComponent), sender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("IllegalIndexComponent"))
                    sender.sendMessage("IllegalIndexComponent");
            }
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");
            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to pick-up a specific eventCard deck, and place current hand component
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param deckIdx idx of event card deck
     * @param sender current sender
     * @throws RemoteException
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

        if(gameManager.getGame().getLevel() == 1){
            sender.sendMessage("CannotPickUpEventCardDeck");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.tryToPlaceComponent(xPlaceComponent, yPlaceComponent);

            ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);

            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);
            sender.sendMessage("AllowedToPlaceComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);

            sender.sendMessage(new PickedUpEventCardDeckMessage(deckIdx, eventCardsDeck));
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");

            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");

            else if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                sender.sendMessage("EventCardDeckIsAlreadyTaken");

            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Handles player decision to pick a booked component, and place current hand component
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param idx idx of booked component's array
     * @param sender current sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndPickBookedComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            sender.sendMessage("AllowedToPlaceComponent");
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);

                gameManager.getGame().getPlayersCopy().get(gameManager.getGame().getPlayersCopy().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);
                String pickedComponentImg = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

                sender.sendMessage("PickedBookedComponent");
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx, pickedComponentImg), sender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("FullHandComponent"))
                    sender.sendMessage("FullHandComponent");
                else if (e.getMessage().equals("IllegalIndex"))
                    sender.sendMessage("IllegalIndex");
                else if (e.getMessage().equals("EmptyBookedCell"))
                    sender.sendMessage("EmptyBookedCell");
            }
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");
            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Try to place component and set player as ready
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param sender current sender
     * @throws RemoteException
     */
    public static void placeHandComponentAndReady(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);
            sender.sendMessage("AllowedToPlaceComponent");

            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName()), sender);

            sender.sendMessage("YouAreReady");
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerIsReadyMessage(player.getName()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.addNotCheckedReadyPlayer(player);
            gameManager.getGameThread().notifyThread();

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                sender.sendMessage("NotAllowedToPlaceComponent");
            else if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else
                sender.sendMessage(e.getMessage());
        }
    }

    /**
     * Set player as ready in building
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void readyBuilding(GameManager gameManager, Player player, Sender sender) throws RemoteException{

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

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

        player.setIsReady(true, gameManager.getGame());
        gameManager.addNotCheckedReadyPlayer(player);
        gameManager.getGameThread().notifyThread();
    }

    /**
     * Handles the player decision to discard its hand component
     *
     * @author Lorenzo
     * @param gameManager is the class that manage the current game
     * @param player is the one that want to discard a cart
     * @param sender current sender
     * @throws RemoteException
     */
    public static void discardComponent(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

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
            if(e.getMessage().equals("HasBeenBooked"))
                sender.sendMessage("HasBeenBooked");
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
     * @param sender current sender
     * @throws RemoteException
     */
    public static void bookComponent(GameManager gameManager, Player player, int idx, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

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
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBookedComponentMessage(player.getName(), imgSrc, idx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                sender.sendMessage("EmptyHandComponent");
            else if (e.getMessage().equals("IllegalBookIndex"))
                sender.sendMessage("IllegalBookIndex");
            else if (e.getMessage().equals("BookedCellOccupied"))
                sender.sendMessage("BookedCellOccupied");
        }
    }

    /**
     * Handles player decision to show booked components
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showBookedComponents(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

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
     * @param sender current sender
     * @throws RemoteException
     */
    public static void pickBookedComponent(GameManager gameManager, Player player, int idx, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if(playerCheck != null) {
                if (playerCheck.equals(player)) {
                    sender.sendMessage("FullHandEventDeck");
                    return;
                }
            }
        }

        try{
            player.getSpaceship().getBuildingBoard().pickBookedComponent(idx);
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();

            sender.sendMessage("PickedBookedComponent");
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx, imgSrc), sender);

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
     * @param gameManager current gameManager
     * @param player current player
     * @param deckIdx idx of event card deck
     * @param sender current sender
     * @throws RemoteException
     */
    public static void pickUpEventCardDeck(GameManager gameManager, Player player, int deckIdx, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        if(player.getIsReady()){
            sender.sendMessage("ActionNotAllowedInReadyState");
            return;
        }

        if(gameManager.getTimerExpired()){
            sender.sendMessage("TimerExpired");
            return;
        }

        if(player.getSpaceship().getBuildingBoard().getHandComponent() != null){
            sender.sendMessage("FullHandComponent");
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if(playerCheck != null) {
                if (playerCheck.equals(player)) {
                    sender.sendMessage("FullHandEventDeck");
                    return;
                }
            }
        }

        if(gameManager.getGame().getLevel() == 1){
            sender.sendMessage("CannotPickUpEventCardDeck");
            return;
        }

        if(player.getSpaceship().getShipComponentsCount() == 1){
            sender.sendMessage("RequirePlacedComponent");
            return;
        }

        try{
            ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);
            sender.sendMessage(new PickedUpEventCardDeckMessage(deckIdx, eventCardsDeck));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

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
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     * @throws RemoteException
     */
    public static void putDownEventCardDeck(GameManager gameManager, Player player, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

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
     * @param gameManager current gameManager
     * @param sender current sender
     * @throws RemoteException
     */
    public static void resetTimer(GameManager gameManager, Sender sender) throws RemoteException{

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            sender.sendMessage("IncorrectPhase");
            return;
        }

        try {
            gameManager.getTimerController().resetTimer();
        }catch (IllegalStateException e){
            if(e.getMessage().equals("ImpossibleToResetTimer"))
                sender.sendMessage("ImpossibleToResetTimer");
        }
    }

    /**
     * Checks the validity of the spaceship for each not ready player.
     * If it's valid, it adds the player to the travelers
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @return areAllValid
     */
    public static boolean checkAllNotReadyStartShipValidityAndAddToTravelers(GameManager gameManager) {
        Game game = gameManager.getGame();
        boolean areAllValid = true;

        for (Player player : gameManager.getCheckedNotReadyPlayersCopy()) {

            Pair<Boolean, Boolean> result = player.getSpaceship().getBuildingBoard().checkStartShipValidity();
            Sender sender = gameManager.getSenderByPlayer(player);

            if(result.getValue()){
                try {
                    if(sender != null)
                        sender.sendMessage("Some components not connected to the central unit have been removed");
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }

            if(result.getKey()){

                gameManager.removeNotCheckedReadyPlayer(player);
                game.getBoard().addTraveler(player);

                if(sender == null)
                    continue;

                try {
                    sender.sendMessage("ValidSpaceShip");
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }else{

                areAllValid = false;
                player.setIsReady(false, game);

                if(sender == null)
                    continue;

                try {
                    sender.sendMessage("NotValidSpaceShip");
                    SpaceshipController.showSpaceship(gameManager, player.getName(), sender);
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }
        }
        return areAllValid;
    }

    public static void checkStartShipValidityController(GameManager gameManager, Player player) {

        Game game = gameManager.getGame();

        Pair<Boolean, Boolean> result = player.getSpaceship().getBuildingBoard().checkStartShipValidity();
        Sender sender = gameManager.getSenderByPlayer(player);

        if(result.getValue()){
            try {
                if(sender != null)
                    sender.sendMessage("Some components not connected to the central unit have been removed");
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }
        }

        if(result.getKey()){

            player.setIsReady(true, game);
            gameManager.removeNotCheckedReadyPlayer(player);
            game.getBoard().addTraveler(player);

            if(sender == null)
                return;

            try {
                sender.sendMessage("ValidSpaceShip");
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }

            gameManager.getGameThread().notifyThread();

        }else{
            player.setIsReady(false, game);

            if(sender == null)
                return;

            try {
                sender.sendMessage("NotValidSpaceShip");
                SpaceshipController.showSpaceship(gameManager, player.getName(), sender);
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }
        }
    }

    /**
     * Try to initialize all spaceship
     *
     * @author Alessandro
     * @param game current game
     * @return areAllInit
     */
    public static boolean initializeAllSpaceship(Game game){

        boolean areAllInit = true;

        for(Player player : game.getPlayersCopy()){

            if(!player.getSpaceship().getBuildingBoard().initSpaceshipParams()){
                player.setIsReady(false, game);
                areAllInit = false;
            }
        }

        return areAllInit;
    }

    public static void autoReadyBuildingForDisconnectedPlayers(GameManager gameManager){

        for(Player player : gameManager.getDisconnectedPlayersCopy()){

            if(!player.getIsReady()){
                player.setIsReady(true, gameManager.getGame());
                gameManager.addNotCheckedReadyPlayer(player);
            }
        }
    }

    public static void kickOutDisconnectedPlayersWithIllegalSpaceship(GameManager gameManager){

        for(Player player : gameManager.getDisconnectedPlayersCopy()){

            Pair<Boolean, Boolean> result = player.getSpaceship().getBuildingBoard().checkStartShipValidity();

            if(!result.getKey()) {
                gameManager.kickOutPlayer(player);
            }
        }
    }
}
