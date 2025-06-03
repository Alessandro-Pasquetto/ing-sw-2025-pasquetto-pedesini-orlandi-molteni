package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.AnotherPlayerDestroyedComponentMessage;
import org.progetto.messages.toClient.DestroyedComponentMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipStatsMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.*;
import org.progetto.server.model.components.*;


/**
 * This controller handles:
 * 1. broadcast updates of spaceship attributes
 * 2. destroy components and validity
 */
public class SpaceshipController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Sends the owner's spaceship to the player that request it
     *
     * @author Lorenzo
     * @param gameManager of the current game
     * @param player owner of the spaceship requested
     */
    public static void showSpaceship(GameManager gameManager, String player, Sender sender) {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        try {
            Player owner = gameManager.getGame().getPlayerByName(player);
            MessageSenderService.sendMessage(new ResponseSpaceshipMessage(owner.getSpaceship(), owner), sender);

        }catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                MessageSenderService.sendMessage("PlayerNameNotFound", sender);
        }
    }

    /**
     * Sends the spaceship to the player that request it
     *
     * @author Gabriele
     * @param gameManager of the current game
     * @param player owner of the spaceship requested
     */
    public static void spaceshipStats(GameManager gameManager, Player player, Sender sender) {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) && !(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) && !(gameManager.getGame().getPhase().equals(GamePhase.POSITIONING)) && !(gameManager.getGame().getPhase().equals(GamePhase.EVENT)) && !(gameManager.getGame().getPhase().equals(GamePhase.TRAVEL))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        try {
            Spaceship spaceship = player.getSpaceship();
            MessageSenderService.sendMessage(new ResponseSpaceshipStatsMessage(spaceship), sender);

        }catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                MessageSenderService.sendMessage("PlayerNameNotFound", sender);
        }
    }

    /**
     * Handles the player decision to move a box between boxStorages
     *
     * @author Gabriele
     * @param gameManager of the current game
     * @param player is the player that needs to move a box
     * @param startY coordinate of starting component
     * @param startX coordinate of starting component
     * @param startIdx box index of starting component
     * @param endY coordinate of final component
     * @param endX coordinate of final component
     * @param endIdx box index of final component
     * @param sender current sender
     */
    public static void moveBox(GameManager gameManager, Player player, int startX, int startY, int startIdx, int endX, int endY, int endIdx, Sender sender) {

        EventPhase phase = gameManager.getEventController().getPhase();
        Player activePlayer = gameManager.getGame().getActivePlayer();

        // Checks if current player in event card during "CHOOSE_BOX" phase is calling this method
        if (!activePlayer.equals(player) || !phase.equals(EventPhase.CHOOSE_BOX)) {
            MessageSenderService.sendMessage("PermissionDenied", sender);
            return;
        }

        try {
            BoxStorage startComponent = (BoxStorage) player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy()[startY][startX];
            BoxStorage endComponent = (BoxStorage) player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy()[endY][endX];

            Box box = startComponent.getBoxes()[startIdx];

            endComponent.tryToAddBox(box, endIdx);
            startComponent.removeBox(player.getSpaceship(), startIdx);
            endComponent.addBox(player.getSpaceship(), box, endIdx); // safe, already validated by tryToAddBox

            MessageSenderService.sendMessage("BoxMoved", sender);

        } catch (ClassCastException e) {
            MessageSenderService.sendMessage("NotAStorageComponent", sender);

        } catch (ArrayIndexOutOfBoundsException e) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }

    /**
     * Handles the player decision to remove a box
     *
     * @author Gabriele
     * @param gameManager current gameManager
     * @param player current player
     * @param yBoxStorage y coordinate of chosen box storage
     * @param xBoxStorage x coordinate of chosen box storage
     * @param idx idx of chosen box storage
     * @param sender current sender
     */
    public static void removeBox(GameManager gameManager, Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) {

        EventPhase phase = gameManager.getEventController().getPhase();
        Player activePlayer = gameManager.getGame().getActivePlayer();

        // Checks if current player in event card during "CHOOSE_BOX" phase is calling this method
        if (!activePlayer.equals(player) || !phase.equals(EventPhase.CHOOSE_BOX)) {
            MessageSenderService.sendMessage("PermissionDenied", sender);
            return;
        }

        try {
            BoxStorage component = (BoxStorage) player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy()[yBoxStorage][xBoxStorage];

            component.removeBox(player.getSpaceship(), idx);

            MessageSenderService.sendMessage("BoxRemoved", sender);

        } catch (ClassCastException e) {
            MessageSenderService.sendMessage("NotAStorageComponent", sender);

        } catch (ArrayIndexOutOfBoundsException e) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }

    /**
     * Called after a component is destroyed by an event and return false if the player has to choose a spaceship part to keep
     *
     * @author Lorenzo, Alessandro
     * @param gameManager of the current game
     * @param player owner of the spaceship
     * @param yComponent coordinate for the destroyed component
     * @param xComponent coordinate fot the destroyed component
     * @param sender current sender
     * @return false if is required to select spaceship part
     */
    public static boolean destroyComponentAndCheckValidity(GameManager gameManager, Player player, int xComponent, int yComponent, Sender sender) {

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try {
            buildingBoard.destroyComponent(xComponent, yComponent);
        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
            return true;
        }

        MessageSenderService.sendMessage(new DestroyedComponentMessage(xComponent, yComponent), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), xComponent, yComponent), sender);

        gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

        // Checks ship validity
        return player.getSpaceship().getBuildingBoard().checkShipValidityAndFixAliens();
    }

    /**
     * Called after in the firstAdjusting
     *
     * @author Alessandro
     * @param gameManager of the current game
     * @param player owner of the spaceship
     * @param yComponent coordinate for the destroyed component
     * @param xComponent coordinate fot the destroyed component
     * @param sender current sender
     */
    public static void startDestroyComponent(GameManager gameManager, Player player, int xComponent, int yComponent, Sender sender) {

        if (!(gameManager.getGame().getPhase().equals(GamePhase.ADJUSTING))) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        try{
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

            if (buildingBoard.getSpaceshipMatrixCopy()[yComponent][xComponent] == null){
                MessageSenderService.sendMessage("EmptyComponentCell", sender);
                return;
            }

            // Checks if player is trying to destroy central unit
            if (buildingBoard.getSpaceshipMatrixCopy()[yComponent][xComponent].getType().equals(ComponentType.CENTRAL_UNIT)) {
                MessageSenderService.sendMessage("ImpossibleToDestroyCentralUnit", sender);
                return;
            }

            // Checks if player is trying to destroy a correct component
            if (!buildingBoard.getSpaceshipMatrixCopy()[yComponent][xComponent].getIncorrectlyPlaced()) {
                MessageSenderService.sendMessage("ImpossibleToDestroyCorrectlyPlaced", sender);
                return;
            }

            buildingBoard.startDestroyComponent(xComponent, yComponent);

            MessageSenderService.sendMessage(new DestroyedComponentMessage(xComponent, yComponent), sender);
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), xComponent, yComponent), sender);

            BuildingController.checkStartShipValidityControllerAndAddToTravelers(gameManager, player);

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }

    /**
     * Player selects a component, we receive its coordinates, then dfs to find the other connected components
     *
     * @author Alessandro
     * @param gameManager of the current game
     * @param player that needs to fix the spaceship
     * @param xComponent x coordinate of chosen component
     * @param yComponent y coordinate of chosen component
     */
    public static void chooseSpaceshipPartToKeep(GameManager gameManager, Player player, int xComponent, int yComponent, Sender sender) throws IllegalStateException {

        try {
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

            buildingBoard.keepSpaceshipPart(xComponent, yComponent);

            MessageSenderService.sendMessage("SpaceshipPartKept", sender);

            gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } catch (IllegalStateException e) {
            MessageSenderService.sendMessage(e.getMessage(), sender);
        }
    }
}