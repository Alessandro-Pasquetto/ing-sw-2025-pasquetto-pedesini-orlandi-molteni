package org.progetto.server.controller;

import org.progetto.messages.toClient.Building.AnotherPlayerDestroyedComponentMessage;
import org.progetto.messages.toClient.Building.DestroyedComponentMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.UpdatedSpaceshipMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;

import java.rmi.RemoteException;


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
     * @throws RemoteException
     */
    public static void showSpaceship(GameManager gameManager, String player, Sender sender) throws RemoteException {

        try {
            Player owner = gameManager.getGame().getPlayerByName(player);
            sender.sendMessage(new ResponseSpaceshipMessage(owner.getSpaceship(), owner.getName()));
        }catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                sender.sendMessage("PlayerNameNotFound");
        }
    }

    /**
     * Called after every modification of a component attributes, updates the view of that player and send a broadcast to other players
     *
     * @author Lorenzo
     * @param gameManager of the current game
     * @param player owner of the modified spaceship
     * @param componentToUpdate is the modified component
     * @param sender
     * @throws RemoteException
     */
    public static void updateSpaceship(GameManager gameManager, Player player, Component componentToUpdate, Sender sender) throws RemoteException {

        if ((componentToUpdate instanceof BatteryStorage) || (componentToUpdate instanceof BoxStorage) || (componentToUpdate instanceof HousingUnit)) {

            gameManager.broadcastGameMessage(new UpdatedSpaceshipMessage(player, componentToUpdate));
            sender.sendMessage("SpaceshipUpdated");
        }else{
            sender.sendMessage("NotAnUpdatableComponent");
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
     * @param sender
     * @throws RemoteException
     */
    public static void moveBox(GameManager gameManager, Player player, int startY, int startX, int startIdx, int endY, int endX, int endIdx, Sender sender) throws RemoteException {

        EventPhase phase = gameManager.getEventController().getPhase();
        Player activePlayer = gameManager.getGame().getActivePlayer();

        // Checks if current player in event card during "CHOOSE_BOX" phase is calling this method
        if (activePlayer.equals(player) && phase.equals(EventPhase.CHOOSE_BOX)) {

            // Checks if start position and end position are the same
            if (startX == endX && startY == endY && startIdx == endIdx) {
                sender.sendMessage("BoxAlreadyThere");

            } else {

                try {
                    BoxStorage startComponent = (BoxStorage) player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[startY][startX];
                    BoxStorage endComponent = (BoxStorage) player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[endY][endX];

                    Box box = startComponent.getBoxStorage()[startIdx];

                    // Checks if the box to move is a red one
                    if (box.getValue() == 4) {
                        if (endComponent.getType().equals(ComponentType.RED_BOX_STORAGE)) {

                            if (endComponent.addBox(player.getSpaceship(), box, endIdx)) {
                                if (startComponent.removeBox(player.getSpaceship(), startIdx)) {
                                    sender.sendMessage("RedBoxMoved");
                                }

                            } else {
                                sender.sendMessage("RedBoxNotMoved");
                            }

                        } else {
                            sender.sendMessage("CantStoreInANonRedStorage");
                        }

                    } else {

                        if (endComponent.addBox(player.getSpaceship(), box, endIdx)) {
                            if (startComponent.removeBox(player.getSpaceship(), startIdx)) {
                                sender.sendMessage("BoxMoved");
                            }

                        } else {
                            sender.sendMessage("BoxNotMoved");
                        }
                    }

                    // Spaceship Updates
                    updateSpaceship(gameManager, player, startComponent, sender);
                    updateSpaceship(gameManager, player, endComponent, sender);

                } catch (ClassCastException e) {
                    sender.sendMessage("NotAStorageComponent");

                } catch (ArrayIndexOutOfBoundsException e) {
                    sender.sendMessage("InvalidCoordinates");
                }
            }

        } else {
            sender.sendMessage("PermissionDenied");
        }
    }

    /**
     * Handles the player decision to remove a box
     *
     * @author Gabriele
     * @param gameManager
     * @param player
     * @param storageY
     * @param storageX
     * @param idx
     * @param sender
     * @throws RemoteException
     */
    public static void removeBox(GameManager gameManager, Player player, int storageY, int storageX, int idx, Sender sender) throws RemoteException {

        EventPhase phase = gameManager.getEventController().getPhase();
        Player activePlayer = gameManager.getGame().getActivePlayer();

        // Checks if current player in event card during "CHOOSE_BOX" phase is calling this method
        if (activePlayer.equals(player) && phase.equals(EventPhase.CHOOSE_BOX)) {

            try {
                BoxStorage component = (BoxStorage) player.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix()[storageY][storageX];

                // Removes selected box
                if (component.removeBox(player.getSpaceship(), idx)) {
                    sender.sendMessage("BoxRemoved");

                } else {
                    sender.sendMessage("BoxNotRemoved");
                }

                // Spaceship Updates
                updateSpaceship(gameManager, player, component, sender);

            } catch (ClassCastException e) {
                sender.sendMessage("NotAStorageComponent");

            } catch (ArrayIndexOutOfBoundsException e) {
                sender.sendMessage("InvalidCoordinates");
            }

        } else {
            sender.sendMessage("PermissionDenied");
        }
    }

    /**
     * Called after a component is destroyed by an event if it's possible auto adjustSpaceship otherwise send message to player to choose a spaceship part to keep
     *
     * @author Lorenzo, Alessandro
     * @param gameManager of the current game
     * @param player owner of the spaceship
     * @param yComponent coordinate for the destroyed component
     * @param xComponent coordinate fot the destroyed component
     * @param sender
     * @throws RemoteException
     */
    public static void destroyComponentAndCheckValidity(GameManager gameManager, Player player, int xComponent, int yComponent, Sender sender) throws RemoteException {

        try{
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
            buildingBoard.destroyComponent(xComponent, yComponent);

            sender.sendMessage(new DestroyedComponentMessage(xComponent, yComponent));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), xComponent, yComponent), sender);

            // Checks ship validity
            if (!player.getSpaceship().getBuildingBoard().checkShipValidityAndTryToFix())
                sender.sendMessage("SpaceshipNotValidSelectPart");

        } catch (IllegalStateException e) {
            if (e.getMessage().equals("EmptyComponentCell"))
                sender.sendMessage("EmptyComponentCell");

            if (e.getMessage().equals("EmptySpaceship"))
                sender.sendMessage("EmptySpaceship");
        }
    }

    /**
     * Player selects a component, we receive its coordinates, then dfs to find the other connected components
     *
     * @author Alessandro
     * @param gameManager of the current game
     * @param player that needs to fix the spaceship
     * @param sender
     * @throws RemoteException
     */
    public static void chooseSpaceshipPartToKeep(GameManager gameManager, Player player, int xComponent, int yComponent, Sender sender) throws RemoteException {

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        try{
            buildingBoard.keepSpaceshipPart(xComponent, yComponent);

            //todo notificare spaceship
            //gameManager.broadcastGameMessageToOthers();
        }catch (IllegalStateException e){
            if(e.getMessage().equals("NotValidCoordinates"))
                sender.sendMessage("NotValidCoordinates");
        }
    }
}