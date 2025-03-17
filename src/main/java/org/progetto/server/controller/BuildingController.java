package org.progetto.server.controller;

import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;

import java.util.function.Consumer;

public class BuildingController {

    public static void handle(Consumer<String> broadcastMessageFunction, Consumer<String> sendMessageToPlayer, Game game, Player player, String message) {

        switch (message){
            case "PickComponent":
                try{
                    Component pickedComponent = game.pickHiddenComponent(player);
                    broadcastMessageFunction.accept("P# " + player.getName() + " picked component: " + pickedComponent.getImgSrc());
                } catch (IllegalStateException e) {
                    if(e.getMessage().equals("HandComponent already set"))
                        sendMessageToPlayer.accept("Hai già un componente in mano");

                    if(e.getMessage().equals("Empty componentDeck")) {
                        sendMessageToPlayer.accept("Non ci sono più componenti da pescare");
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