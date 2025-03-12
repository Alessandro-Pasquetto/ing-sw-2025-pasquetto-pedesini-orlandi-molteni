package org.progetto.server.controller;

import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;

import java.util.function.Consumer;

public class BuildingController {

    public static void handle(Consumer<String> broadcastMessageFunction, Consumer<String> sendMessageToPlayer, Game game, Player player, String message) {

        switch (message){
            case "PickComponent":
                if(game.getComponentDeckSize() > 0){

                    if(player.getSpaceship().getBuildingBoard().getHandComponent() == null){
                        Component c = game.pickComponent(player);
                        broadcastMessageFunction.accept(player.getName() + " picked component " + c.toString());
                    }else{
                        sendMessageToPlayer.accept("Hai già un componente in mano");
                    }
                }else{
                    sendMessageToPlayer.accept("Non ci sono più componenti da pescare");
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