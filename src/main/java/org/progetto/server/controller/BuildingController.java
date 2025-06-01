package org.progetto.server.controller;

import javafx.util.Pair;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if (player.getIsReady()) {
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if (gameManager.getTimerExpired()) {
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try {
            Component handComponent = player.getSpaceship().getBuildingBoard().getHandComponent();

            if (handComponent != null) {
                MessageSenderService.sendOptional(new ShowHandComponentMessage(handComponent), sender);
            } else {
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            }

        } catch (IllegalStateException e) {
            MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if(player.equals(playerCheck)) {
                MessageSenderService.sendOptional("FullHandEventDeck", sender);
                return;
            }
        }

        try{
            Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
            MessageSenderService.sendOptional(new PickedComponentMessage(pickedComponent), sender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                MessageSenderService.sendOptional("FullHandComponent", sender);

            if(e.getMessage().equals("EmptyComponentDeck"))
                MessageSenderService.sendOptional("EmptyComponentDeck", sender);

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
     */
    public static void buildShip(GameManager gameManager, Player player, int idShip, Sender sender) {
        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try{

            int spaceshipLevel = player.getSpaceship().getLevelShip();
            BuildingBoard bb = player.getSpaceship().getBuildingBoard();

            // Level 1 ships
            if (spaceshipLevel == 1) {

                switch (idShip) {
                    case 1:
                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{2, 2, 1, 0}, "battery2.jpg", 3));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 0, 2, 0}, "double-cannon10.jpg"));
                        bb.placeComponent(2, 0, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 1, 2, 1}, "shield3.jpg"));
                        bb.placeComponent(1, 2, 3);

                        bb.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{0, 0, 2, 3}, "purple-alien-housing-unit4.jpg"));
                        bb.placeComponent(0, 2, 3);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{2, 1, 0, 1}, "housing-unit5.jpg", 2));
                        bb.placeComponent(0, 3, 1);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 2, 1, 0}, "battery15.jpg", 2));
                        bb.placeComponent(1, 3, 2);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 0, 0, 0}, "single-engine10.jpg"));
                        bb.placeComponent(0, 4, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{0, 0, 0, 3}, "blue-box-storage13.jpg", 2));
                        bb.placeComponent(1, 4, 1);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 1}, "housing-unit1.jpg", 2));
                        bb.placeComponent(2, 3, 2);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{2, 1, 0, 3}, "red-box-storage6.jpg", 1));
                        bb.placeComponent(3, 2, 2);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 0, 0, 3}, "double-cannon2.jpg"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 1, 3, 1}, "shield1.jpg"));
                        bb.placeComponent(3, 3, 2);

                        bb.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{1, 1, 0, 1}, "orange-alien-housing-unit1.jpg"));
                        bb.placeComponent(4, 3, 3);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 1}, "housing-unit1.jpg", 2));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 0, 0, 2}, "double-engine3.jpg"));
                        bb.placeComponent(4, 4, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 2, 0, 3}, "housing-unit13.jpg", 2));
                        bb.placeComponent(3, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 1, 0}, "single-cannon2.jpg"));
                        bb.placeComponent(1, 1, 0);

                        gameManager.broadcastGameMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    case 2:
                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{2, 2, 1, 0}, "battery2.jpg", 3));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 2, 0}, "single-cannon4.jpg"));
                        bb.placeComponent(2, 0, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 2, 1, 1}, "housing-unit2.jpg", 2));
                        bb.placeComponent(1, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 1, 0}, "single-cannon2.jpg"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 0, 0, 1}, "battery13.jpg", 2));
                        bb.placeComponent(0, 2, 2);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 0, 0}, "single-engine12.jpg"));
                        bb.placeComponent(0, 3, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 0, 0, 3}, "blue-box-storage8.jpg", 2));
                        bb.placeComponent(1, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{1, 3, 0, 0}, "double-engine8.jpg"));
                        bb.placeComponent(2, 3, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{0, 2, 0, 1}, "red-box-storage2.jpg", 2));
                        bb.placeComponent(3, 2, 2);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 0, 0, 3}, "double-cannon2.jpg"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 1, 0, 3}, "housing-unit11.jpg", 2));
                        bb.placeComponent(3, 3, 2);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 0, 0, 3}, "single-engine1.jpg"));
                        bb.placeComponent(4, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 1, 3, 1}, "shield1.jpg"));
                        bb.placeComponent(4, 2, 0);

                        gameManager.broadcastGameMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    default:
                        MessageSenderService.sendOptional("IDShipOutOfBounds", sender);
                }

            // Level 2 ships
            } else if (spaceshipLevel == 2) {

                switch (idShip) {
                    case 1:
                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 2, 1, 2}, "double-cannon3.jpg"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 0, 0, 3}, "double-cannon2.jpg"));
                        bb.placeComponent(4, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 2, 0, 3}, "single-cannon1.jpg"));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 1, 0, 0}, "single-cannon6.jpg"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{2, 1, 0, 1}, "housing-unit5.jpg", 2));
                        bb.placeComponent(2, 2, 2);

                        bb.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{2, 0, 0, 3}, "purple-alien-housing-unit6.jpg"));
                        bb.placeComponent(1, 2, 2);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 1, 3, 2}, "housing-unit7.jpg", 2));
                        bb.placeComponent(1, 3, 1);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 0, 0, 3}, "red-box-storage4.jpg", 1));
                        bb.placeComponent(0, 3, 1);

                        // TODO: remove
                        BoxStorage bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{0, 0, 0, 2}, "blue-box-storage5.jpg", 3);
                        bs.addBox(player.getSpaceship(), Box.YELLOW, 0);
                        bs.addBox(player.getSpaceship(), Box.BLUE, 1);
                        bs.addBox(player.getSpaceship(), Box.GREEN, 2);
                        bb.setHandComponent(bs);
                        bb.placeComponent(0, 2, 3);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 0, 0}, "single-engine12.jpg"));
                        bb.placeComponent(1, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{0, 3, 0, 3}, "double-engine2.jpg"));
                        bb.placeComponent(2, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 2}, "shield4.jpg"));
                        bb.placeComponent(2, 3, 2);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{2, 2, 1, 0}, "battery2.jpg", 3));
                        bb.placeComponent(3, 3, 3);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 1, 3, 1}, "shield1.jpg"));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 1, 1, 1}, "battery12.jpg", 2));
                        bb.placeComponent(4, 3, 1);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 2, 1, 1}, "housing-unit2.jpg", 2));
                        bb.placeComponent(5, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{1, 1, 0, 1}, "orange-alien-housing-unit1.jpg"));
                        bb.placeComponent(5, 2, 3);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 1, 0}, "single-cannon2.jpg"));
                        bb.placeComponent(5, 1, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 0, 0, 3}, "red-box-storage4.jpg", 1));
                        bb.placeComponent(6, 3, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{2, 1, 3, 0}, "blue-box-storage1.jpg", 2));
                        bb.placeComponent(6, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 1, 0, 0}, "double-engine6.jpg"));
                        bb.placeComponent(4, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 0, 0, 3}, "single-engine1.jpg"));
                        bb.placeComponent(5, 4, 0);

                        gameManager.broadcastGameMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    case 2:
                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 2, 3, 1}, "single-cannon17.jpg"));
                        bb.placeComponent(3, 1, 0);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 2}, "shield4.jpg"));
                        bb.placeComponent(4, 1, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 2, 0}, "battery5.jpg", 3));
                        bb.placeComponent(2, 1, 0);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{0, 0, 0, 1}, "battery1.jpg", 3));
                        bb.placeComponent(2, 0, 3);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{2, 1, 0, 3}, "red-box-storage6.jpg", 2));
                        bb.placeComponent(2, 2, 1);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 1}, "housing-unit1.jpg", 2));
                        bb.placeComponent(2, 3, 1);

                        bb.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 0, 1, 1}, "shield2.jpg"));
                        bb.placeComponent(1, 3, 1);

                        bs = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{2, 0, 1, 2}, "blue-box-storage2.jpg", 3);
                        bs.addBox(player.getSpaceship(), Box.YELLOW, 0);
                        bs.addBox(player.getSpaceship(), Box.BLUE, 1);
                        bs.addBox(player.getSpaceship(), Box.GREEN, 2);
                        bb.setHandComponent(bs);
                        bb.placeComponent(1, 2, 0);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 0, 3, 2}, "double-cannon4.jpg"));
                        bb.placeComponent(1, 1, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 1}, "housing-unit1.jpg", 2));
                        bb.placeComponent(0, 2, 3);

                        bb.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{0, 0, 1, 3}, "orange-alien-housing-unit4.jpg"));
                        bb.placeComponent(0, 3, 2);

                        bb.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 0, 0, 3}, "battery7.jpg", 2));
                        bb.placeComponent(3, 3, 1);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 2, 1, 1}, "housing-unit2.jpg", 2));
                        bb.placeComponent(4, 2, 0);

                        bb.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 2, 2, 1}, "housing-unit3.jpg", 2));
                        bb.placeComponent(4, 3, 0);

                        bb.setHandComponent(new Component(ComponentType.ENGINE, new int[]{2, 3, 0, 0}, "single-engine20.jpg"));
                        bb.placeComponent(4, 4, 0);

                        bb.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{1, 2, 0, 2}, "purple-alien-housing-unit1.jpg"));
                        bb.placeComponent(5, 2, 2);

                        bb.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 3, 1}, "single-cannon15.jpg"));
                        bb.placeComponent(5, 3, 1);

                        bb.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{0, 3, 0, 3}, "double-engine2.jpg"));
                        bb.placeComponent(5, 4, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{0, 0, 0, 2}, "red-box-storage1.jpg", 2));
                        bb.placeComponent(6, 2, 0);

                        bb.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{0, 0, 0, 2}, "blue-box-storage5.jpg", 3));
                        bb.placeComponent(6, 4, 0);

                        gameManager.broadcastGameMessage(new ResponseSpaceshipMessage(player.getSpaceship(), player));
                        break;

                    default:
                        MessageSenderService.sendOptional("IDShipOutOfBounds", sender);
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
     * @param sender current sender
     * @throws RemoteException
     */
    public static void showVisibleComponents(GameManager gameManager, Sender sender) throws RemoteException {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try {
            ArrayList<Component> visibleDeck = gameManager.getGame().getVisibleComponentDeckCopy();
            MessageSenderService.sendOptional(new ShowVisibleComponentsMessage(visibleDeck), sender);

        } catch (IllegalStateException e) {
            MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if (player.equals(playerCheck)) {
                MessageSenderService.sendOptional("FullHandEventDeck", sender);
                return;
            }
        }

        try{
            gameManager.getGame().pickVisibleComponent(componentIdx, player);
            Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
            MessageSenderService.sendOptional(new PickedComponentMessage(pickedComponent), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedVisibleComponentMessage(player.getName(), pickedComponent), sender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                MessageSenderService.sendOptional("FullHandComponent", sender);

            if(e.getMessage().equals("IllegalIndexComponent"))
                MessageSenderService.sendOptional("IllegalIndexComponent", sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if (player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if (gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);
            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);
            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
     */
    public static void placeHandComponentAndPickHiddenComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, Sender sender) {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();
            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);
                Component pickedComponent = gameManager.getGame().pickHiddenComponent(player);
                MessageSenderService.sendOptional(new PickedComponentMessage(pickedComponent), sender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("FullHandComponent"))
                    MessageSenderService.sendOptional("FullHandComponent", sender);

                if (e.getMessage().equals("EmptyComponentDeck"))
                    MessageSenderService.sendOptional("EmptyComponentDeck", sender);
            }
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);
            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
     */
    public static void placeHandComponentAndPickVisibleComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent, Sender sender) {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();

            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);
            try{
                gameManager.getGame().pickVisibleComponent(idxVisibleComponent, player);
                Component pickedComponent = player.getSpaceship().getBuildingBoard().getHandComponent();
                MessageSenderService.sendOptional(new PickedComponentMessage(pickedComponent), sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedVisibleComponentMessage(player.getName(), pickedComponent), sender);

            } catch (IllegalStateException e) {
                if (e.getMessage().equals("IllegalIndexComponent"))
                    MessageSenderService.sendOptional("IllegalIndexComponent", sender);
            }
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);
            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
        }
    }

    /**
     * Handles player decision to pick up a specific eventCard deck, and place current hand component
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param xPlaceComponent x coordinate of the position in the spaceship matrix
     * @param yPlaceComponent y coordinate of the position in the spaceship matrix
     * @param rPlaceComponent rotation
     * @param deckIdx idx of event card deck
     * @param sender current sender
     */
    public static void placeHandComponentAndPickUpEventCardDeck(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx, Sender sender) {

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        if(gameManager.getGame().getLevel() == 1){
            MessageSenderService.sendOptional("CannotPickUpEventCardDeck", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();

            buildingBoard.tryToPlaceComponent(xPlaceComponent, yPlaceComponent);

            ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);

            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);
            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);

            MessageSenderService.sendOptional(new PickedUpEventCardDeckMessage(deckIdx, eventCardsDeck), sender);
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);

            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);

            else if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                MessageSenderService.sendOptional("EventCardDeckIsAlreadyTaken", sender);

            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
     */
    public static void placeHandComponentAndPickBookedComponent(GameManager gameManager, Player player, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx, Sender sender) {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();

            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);

            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);
            try{
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);

                gameManager.getGame().getPlayersCopy().get(gameManager.getGame().getPlayersCopy().indexOf(player)).getSpaceship().getBuildingBoard().pickBookedComponent(idx);

                MessageSenderService.sendOptional("PickedBookedComponent", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx), sender);

            } catch (IllegalStateException e) {
                if(e.getMessage().equals("FullHandComponent"))
                    MessageSenderService.sendOptional("FullHandComponent", sender);
                else if (e.getMessage().equals("IllegalIndex"))
                    MessageSenderService.sendOptional("IllegalIndex", sender);
                else if (e.getMessage().equals("EmptyBookedCell"))
                    MessageSenderService.sendOptional("EmptyBookedCell", sender);
            }
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);
            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            Component component = buildingBoard.getHandComponent();

            buildingBoard.placeComponent(xPlaceComponent, yPlaceComponent, rPlaceComponent);
            MessageSenderService.sendOptional("AllowedToPlaceComponent", sender);

            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPlacedComponentMessage(player.getName(), component, xPlaceComponent, yPlaceComponent), sender);

            MessageSenderService.sendOptional("YouAreReady", sender);
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerIsReadyMessage(player.getName()), sender);

            player.setIsReady(true, gameManager.getGame());
            gameManager.addNotCheckedReadyPlayer(player);
            gameManager.getGameThread().notifyThread();

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotAllowedToPlaceComponent"))
                MessageSenderService.sendOptional("NotAllowedToPlaceComponent", sender);
            else if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
        }
    }

    /**
     * Set player as ready in building
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     * @param sender current sender
     */
    public static void readyBuilding(GameManager gameManager, Player player, Sender sender){

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        MessageSenderService.sendOptional("YouAreReady", sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try{
            String imgSrc = gameManager.getGame().discardComponent(player);
            MessageSenderService.sendOptional("HandComponentDiscarded", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDiscardComponentMessage(player.getName(), imgSrc), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            if(e.getMessage().equals("HasBeenBooked"))
                MessageSenderService.sendOptional("HasBeenBooked", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try {
            Component component = player.getSpaceship().getBuildingBoard().getHandComponent();
            player.getSpaceship().getBuildingBoard().setAsBooked(idx);
            MessageSenderService.sendOptional("ComponentBooked", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerBookedComponentMessage(player.getName(), component, idx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EmptyHandComponent"))
                MessageSenderService.sendOptional("EmptyHandComponent", sender);
            else if (e.getMessage().equals("IllegalBookIndex"))
                MessageSenderService.sendOptional("IllegalBookIndex", sender);
            else if (e.getMessage().equals("BookedCellOccupied"))
                MessageSenderService.sendOptional("BookedCellOccupied", sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try {
            Component[] bookedComponents = player.getSpaceship().getBuildingBoard().getBookedCopy();
            MessageSenderService.sendOptional(new ShowBookedComponentsMessage(bookedComponents), sender);

        } catch (IllegalStateException e) {
            MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if(playerCheck != null) {
                if (playerCheck.equals(player)) {
                    MessageSenderService.sendOptional("FullHandEventDeck", sender);
                    return;
                }
            }
        }

        try{
            player.getSpaceship().getBuildingBoard().pickBookedComponent(idx);

            MessageSenderService.sendOptional("PickedBookedComponent", sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedBookedComponentMessage(player.getName(), idx), sender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                MessageSenderService.sendOptional("FullHandComponent", sender);
            else if (e.getMessage().equals("IllegalIndex"))
                MessageSenderService.sendOptional("IllegalIndex", sender);
            else if (e.getMessage().equals("EmptyBookedCell"))
                MessageSenderService.sendOptional("EmptyBookedCell", sender);
        }
    }

    /**
     * Handles player decision to pick up a specific eventCard deck
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        if(player.getSpaceship().getBuildingBoard().getHandComponent() != null){
            MessageSenderService.sendOptional("FullHandComponent", sender);
            return;
        }

        for(Player playerCheck : gameManager.getGame().getEventDeckAvailableCopy()) {
            if(playerCheck != null) {
                if (playerCheck.equals(player)) {
                    MessageSenderService.sendOptional("FullHandEventDeck", sender);
                    return;
                }
            }
        }

        if(gameManager.getGame().getLevel() == 1){
            MessageSenderService.sendOptional("CannotPickUpEventCardDeck", sender);
            return;
        }

        if(player.getSpaceship().getShipComponentsCount() == 1){
            MessageSenderService.sendOptional("RequirePlacedComponent", sender);
            return;
        }

        try{
            ArrayList<EventCard> eventCardsDeck = gameManager.getGame().pickUpEventCardDeck(player, deckIdx);
            MessageSenderService.sendOptional(new PickedUpEventCardDeckMessage(deckIdx, eventCardsDeck), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerPickedUpEventCardDeck(player.getName(), deckIdx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("EventCardDeckIsAlreadyTaken"))
                MessageSenderService.sendOptional("EventCardDeckIsAlreadyTaken", sender);
            if(e.getMessage().equals("IllegalIndexEventCardDeck"))
                MessageSenderService.sendOptional("IllegalIndexEventCardDeck", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(player.getIsReady()){
            MessageSenderService.sendOptional("ActionNotAllowedInReadyState", sender);
            return;
        }

        if(gameManager.getTimerExpired()){
            MessageSenderService.sendOptional("TimerExpired", sender);
            return;
        }

        try{
            int deckIdx = gameManager.getGame().putDownEventCardDeck(player);
            MessageSenderService.sendOptional("EventCardDeckPutDown", sender);
            gameManager.broadcastGameMessageToOthers( new AnotherPlayerPutDownEventCardDeckMessage(player.getName(), deckIdx), sender);

        }catch (IllegalStateException e){
            if(e.getMessage().equals("NoEventCardDeckTaken"))
                MessageSenderService.sendOptional("NoEventCardDeckTaken", sender);
            else
                MessageSenderService.sendOptional(e.getMessage(), sender);
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
    public static void resetTimer(GameManager gameManager, Player player, Sender sender) throws RemoteException{

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))) {
            MessageSenderService.sendOptional("IncorrectPhase", sender);
            return;
        }

        if(gameManager.getTimerController().getTimerFlipsAllowed() == 1 && !player.getIsReady()){
            MessageSenderService.sendOptional("FinalResetNotAllowed", sender);
            return;
        }

        try {
            gameManager.getTimerController().resetTimer();
        }catch (IllegalStateException e){
            if(e.getMessage().equals("ImpossibleToResetTimer"))
                MessageSenderService.sendOptional("ImpossibleToResetTimer", sender);
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

            if (result.getValue()) {
                MessageSenderService.sendOptional("ComponentsNotConnectedGotRemoved", sender);
            }

            if (result.getKey()) {

                gameManager.removeNotCheckedReadyPlayer(player);
                game.getBoard().addTraveler(player);

                MessageSenderService.sendOptional("ValidSpaceShip", sender);

            } else {

                areAllValid = false;
                player.setIsReady(false, game);

                MessageSenderService.sendOptional("NotValidSpaceShip", sender);
                MessageSenderService.sendOptional(new ResponseSpaceshipMessage(player.getSpaceship(), player), sender);
            }
        }
        return areAllValid;
    }

    /**
     * Checks the validity of the spaceship for a player and adds it to the travelers if valid
     *
     * @author Alessandro
     * @param gameManager current gameManager
     * @param player current player
     */
    public static void checkStartShipValidityControllerAndAddToTravelers(GameManager gameManager, Player player) {

        Game game = gameManager.getGame();

        Pair<Boolean, Boolean> result = player.getSpaceship().getBuildingBoard().checkStartShipValidity();
        Sender sender = gameManager.getSenderByPlayer(player);

        if (result.getValue()) {
            MessageSenderService.sendOptional("ComponentsNotConnectedGotRemoved", sender);
        }

        if (result.getKey()) {

            player.setIsReady(true, game);
            gameManager.removeNotCheckedReadyPlayer(player);
            game.getBoard().addTraveler(player);

            MessageSenderService.sendOptional("ValidSpaceShip", sender);

            gameManager.getGameThread().notifyThread();

        } else {
            player.setIsReady(false, game);

            MessageSenderService.sendOptional("NotValidSpaceShip", sender);
            MessageSenderService.sendOptional(new ResponseSpaceshipMessage(player.getSpaceship(), player), sender);
        }
    }

    /**
     * Try to initialize all spaceship
     *
     * @author Alessandro
     * @param game current gameManager
     * @return areAllInit
     */
    public static boolean initializeAllSpaceship(Game game){

        boolean areAllInit = true;

        for(Player player : game.getBoard().getCopyTravelers()){

            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

            if(!buildingBoard.initSpaceshipParams()){
                player.setIsReady(false, game);
                areAllInit = false;
            }
            buildingBoard.removeBookedComponents();

            if(game.getLevel() == 1)
                player.getSpaceship().resetDestroyedCount();
        }

        return areAllInit;
    }

    /**
     * Set all disconnected players as ready
     *
     * @author Alessandro
     * @param gameManager current gameManager
     */
    public static void autoReadyBuildingForDisconnectedPlayers(GameManager gameManager){

        for(Player player : gameManager.getDisconnectedPlayersCopy()){

            if(!player.getIsReady()){
                player.setIsReady(true, gameManager.getGame());
                gameManager.addNotCheckedReadyPlayer(player);
            }
        }
    }

    /**
     * Add disconnected players with illegal spaceship to losing players
     *
     * @author Alessandro
     * @param gameManager current gameManager
     */
    public static void addDisconnectedPlayersWithIllegalSpaceshipToLosingPlayers(GameManager gameManager){

        for(Player player : gameManager.getDisconnectedPlayersCopy()){

            Pair<Boolean, Boolean> result = player.getSpaceship().getBuildingBoard().checkStartShipValidity();

            if(!result.getKey()) {
                gameManager.addLosingPlayer(player);
            }
        }
    }
}
