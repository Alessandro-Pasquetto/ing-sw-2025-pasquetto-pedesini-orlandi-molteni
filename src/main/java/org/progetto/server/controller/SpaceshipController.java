package org.progetto.server.controller;


import com.fasterxml.jackson.core.io.JsonEOFException;
import org.progetto.messages.toClient.Building.AnotherPlayerDestroyedComponentMessage;
import org.progetto.messages.toClient.Building.DestroyedComponentMessage;
import org.progetto.messages.toClient.Spaceship.UpdatedSpaceship;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This controller handles:
 * 1. broadcast updates of spaceship attributes
 * 3. destroy components and validity
 */
public class SpaceshipController {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Called after every modification of a component attributes, updates the view of that player and send a broadcast to other players
     *
     * @author Lorenzo
     * @param gameManager of the current game
     * @param player owner of the modified spaceship
     * @param componentToUpdate is the modified component
     * @param sender
     */
    public static void updateSpaceship(GameManager gameManager, Player player, Component componentToUpdate, Sender sender) throws RemoteException {

        if ((componentToUpdate instanceof BatteryStorage) || (componentToUpdate instanceof BoxStorage) || (componentToUpdate instanceof HousingUnit)) {

            gameManager.broadcastGameMessage(new UpdatedSpaceship(player, componentToUpdate));
            sender.sendMessage("SpaceshipUpdated");
        }else{
            sender.sendMessage("NotAnUpdatableComponent");
        }
    }

    /**
     * Handles the player decision to move a box between boxStorages
     *
     * @author Lorenzo
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

        if (startX == endX && startY == endY && startIdx == endIdx) {
            sender.sendMessage("CantStayStill");
        } else {

            try {
                // Move box removing from start and adding to end
                BoxStorage startComponent = (BoxStorage) player.getSpaceship().getBuildingBoard().getSpaceshipMatrix()[startY][startX];
                BoxStorage endComponent = (BoxStorage) player.getSpaceship().getBuildingBoard().getSpaceshipMatrix()[endY][endX];

                Box box = startComponent.getBoxStorage()[startIdx];

                if (box.getValue() == 4) {
                    if (endComponent.getType().equals(ComponentType.RED_BOX_STORAGE)) {

                        if (startComponent.removeBox(player.getSpaceship(), startIdx)) {
                            if (endComponent.addBox(player.getSpaceship(), box, endIdx))
                                sender.sendMessage("RedBoxMoved");
                            else sender.sendMessage("RedBoxNotAdded");

                        } else sender.sendMessage("RedBoxNotMoved");

                    } else {
                        sender.sendMessage("CantStoreInANonRedStorage");
                    }
                } else {

                    if (startComponent.removeBox(player.getSpaceship(), startIdx)) {
                        if (endComponent.addBox(player.getSpaceship(), box, endIdx))
                            sender.sendMessage("BoxMoved");
                        else sender.sendMessage("BoxNotAdded");

                    } else sender.sendMessage("RedBoxNotMoved");
                }

                // Spaceship Updates
                updateSpaceship(gameManager,player,startComponent,sender);
                updateSpaceship(gameManager,player,endComponent,sender);

            } catch (ClassCastException e) {
                sender.sendMessage("NotAStorageComponent");
            }
        }
    }

    /**
     * Called after a component is destroyed by an event, handles broadcast destruction message
     *
     * @author Lorenzo
     * @param gameManager of the current game
     * @param player owner of the spaceship
     * @param yComponent coordinate for the destroyed component
     * @param xComponent coordinate fot the destroyed component
     * @param sender
     * @throws RemoteException
     */
    public static void destroyComponentAndCheckValidity(GameManager gameManager, Player player, int yComponent, int xComponent, Sender sender) throws RemoteException {

        try{
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
            buildingBoard.destroyComponent(yComponent,xComponent);

            sender.sendMessage(new DestroyedComponentMessage(yComponent, xComponent));
            gameManager.broadcastGameMessageToOthers(new AnotherPlayerDestroyedComponentMessage(player.getName(), yComponent, xComponent), sender);

            // Checks ship validity
            if (!player.getSpaceship().getBuildingBoard().checkShipValidity()){
                sender.sendMessage("SpaceshipValid");
            } else {
                sender.sendMessage("SpaceshipNotValid");  // Handled by view, player sends coordinates to fix ship
            }

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
     * @author Lorenzo
     * @param gameManager of the current game
     * @param player that needs to fix the spaceship
     * @param sender
     * @throws RemoteException
     */
    public static void fixSpaceship(GameManager gameManager, Player player, int yComponent, int xComponent, Sender sender) throws RemoteException {

        Component selectedComponent = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix()[yComponent][xComponent];
        if (selectedComponent != null){

            // Removes components that do not belong to the new spaceship
            Component[][] matrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();

            Set<Component> newSpaceship = new HashSet<>(player.getSpaceship().getBuildingBoard().getNewSpaceship(yComponent, xComponent));

            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    if (matrix[i][j] != null && newSpaceship.contains(matrix[i][j])) {
                       player.getSpaceship().getBuildingBoard().destroyComponent(i,j);

                       destroyComponentAndCheckValidity(gameManager, player, yComponent, xComponent, sender);  // Notifies the destruction
                    }
                }
            }

            // New ship should be ready to use

        } else {
            sender.sendMessage("InvalidSelection");
        }

    }
}