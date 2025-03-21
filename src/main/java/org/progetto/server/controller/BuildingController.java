package org.progetto.server.controller;

import org.progetto.messages.AnotherPlayerPlacedComponentMessage;
import org.progetto.messages.PickedComponentMessage;
import org.progetto.messages.PlaceHandComponentAndPickComponentMessage;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;

import java.util.function.Consumer;

public class BuildingController {

    public static void handle(Consumer<Object> broadcastMessageFunction, SocketWriter socketWriter, Game game, Player player, Object messageObj) {

        if(messageObj instanceof PlaceHandComponentAndPickComponentMessage placeHandComponentAndPickComponentMessage) {
            int yPlaceComponent = placeHandComponentAndPickComponentMessage.getY();
            int xPlaceComponent = placeHandComponentAndPickComponentMessage.getX();
            int rPlaceComponent = placeHandComponentAndPickComponentMessage.getRotation();
            String imgSrc = player.getSpaceship().getBuildingBoard().getHandComponent().getImgSrc();
            if(player.getSpaceship().getBuildingBoard().placeComponent(yPlaceComponent, xPlaceComponent, rPlaceComponent)){
                try{
                    socketWriter.sendMessageToOtherPlayersInGame(new AnotherPlayerPlacedComponentMessage(player.getName(), xPlaceComponent, yPlaceComponent, rPlaceComponent, imgSrc));
                    Component pickedComponent = game.pickHiddenComponent(player);
                    socketWriter.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));
                } catch (IllegalStateException e) {
                    if(e.getMessage().equals("HandComponent already set"))
                        socketWriter.sendMessage("HandComponentFull");

                    if(e.getMessage().equals("Empty componentDeck")) {
                        socketWriter.sendMessage("EmptyComponentDeck");
                    }
                }
            }else
                socketWriter.sendMessage("ImpossiblePlaceComponent");

        } else if(messageObj instanceof String messageString){
            switch (messageString){
                case "PickComponent":
                    try{
                        Component pickedComponent = game.pickHiddenComponent(player);
                        socketWriter.sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()));
                    } catch (IllegalStateException e) {
                        if(e.getMessage().equals("HandComponent already set"))
                            socketWriter.sendMessage("HandComponentFull");

                        if(e.getMessage().equals("Empty componentDeck")) {
                            socketWriter.sendMessage("EmptyComponentDeck");
                        }
                    }
                    break;
                case "Right":
                    System.out.println("Right");
                    break;
                case "Left":
                    System.out.println("Left");
                    break;
                default:
                    break;
            }
        }
    }
}