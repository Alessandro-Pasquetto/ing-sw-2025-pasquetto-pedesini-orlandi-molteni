package org.progetto.server.controller;

import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;

import java.util.function.Consumer;

public class InitController {

    public static void handle(Runnable startTimerFunction, Consumer<String> broadcastMessageFunction, Game game, Player player, String message) {
        switch (message){
            case "StartGame":
                System.out.println("StartGame");
                broadcastMessageFunction.accept("StartGame");

                game.setPhase(GamePhase.BUILDING);

                startTimerFunction.run();

                break;
            default:
                break;
        }
    }
}