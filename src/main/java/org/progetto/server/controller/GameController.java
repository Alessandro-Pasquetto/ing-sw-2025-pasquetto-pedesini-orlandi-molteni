package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualView;
import org.progetto.messages.toClient.AnotherPlayerPlacedComponentMessage;
import org.progetto.messages.toClient.PickedComponentMessage;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.Game;
import org.progetto.server.model.components.Component;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController {

    private static final AtomicInteger currentIdGame = new AtomicInteger(0);

    private static void sendMessage(Object message, SocketWriter swSender, VirtualView vvSender){
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

    public static void pickHiddenComponent(Game game, Player player, SocketWriter swSender, VirtualView vvSender){
        try{
            Component pickedComponent = game.pickHiddenComponent(player);
            sendMessage(new PickedComponentMessage(pickedComponent.getImgSrc()), swSender, vvSender);

        } catch (IllegalStateException e) {
            if(e.getMessage().equals("FullHandComponent"))
                sendMessage("FullHandComponent", swSender, vvSender);

            if(e.getMessage().equals("EmptyComponentDeck"))
                sendMessage("EmptyComponentDeck", swSender, vvSender);
        }
    }

    public static void placeHandComponentAndPickHiddenComponent(GameManager gameManager, Player player, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, SocketWriter swSender, VirtualView vvSender) {
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
}