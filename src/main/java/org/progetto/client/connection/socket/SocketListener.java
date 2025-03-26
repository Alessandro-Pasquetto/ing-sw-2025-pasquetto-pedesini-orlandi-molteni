package org.progetto.client.connection.socket;

import org.progetto.client.PageController;
import org.progetto.messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class SocketListener extends Thread {

    private static ObjectInputStream in;
    private static boolean running = true;

    public SocketListener(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Object messageObj = in.readObject();
                handlerMessage(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            stopListener();
        }
    }

    private void handlerMessage(Object messageObj) {

        if (messageObj instanceof GameListMessage gameListMessage) {
            ArrayList<Integer> idGames = gameListMessage.getIdGames();
            System.out.println("Gamelist arrivata... da elaborare");
        }
        else if (messageObj instanceof NotifyNewGameMessage notifyNewGameMessage) {
            PageController.generateGameList(notifyNewGameMessage.getIdGame());

        } else if (messageObj instanceof InitGameMessage initGameMessage) {
            PageController.initGame(initGameMessage.getImgPathBoard(), initGameMessage.getImgPathSpaceship(), initGameMessage.getImgPathCentralUnit());

        } else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            PageController.generateComponent(pickedComponentMessage.getImgPath());

        } else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            System.out.println(anotherPlayerPlacedComponentMessage.getNamePlayer() + " has placed: " + anotherPlayerPlacedComponentMessage.getImgSrcPlacedComponent());

        } else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.gameView.updateTimer(timer);

        } else if (messageObj instanceof String messageString) {

            switch (messageString) {
                case "AllowedToJoinGame":
                    SocketClient.joinToGame();
                    break;
                case "NotAllowedToJoinGame":
                    System.out.println("Username not available");
                    break;
                case "NotAllowedToPlaceComponent":
                    break;
                case "Timer expired":
                    break;
                default:
                    System.out.println(messageString);
                    break;
            }
        }
    }

    public static void stopListener() {
        running = false;
        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}