package org.progetto.client.connection;

import java.io.IOException;

public interface Sender {
    void connect(String serverIp, int serverPort);

    void createGame();

    void tryJoinToGame(int idGame);

    void startGame();

    void pickHiddenComponent();

    void pickVisibleComponent();

    void placeLastComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeHandComponentAndPickHiddenComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeHandComponentAndPickVisibleComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int componentIdx);

    void placeHandComponentAndPickUpEventCardDeck(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx);

    void placeHandComponentAndPickBookedComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx);

    void discardComponent();

    void bookComponent(int idx);

    void pickBookedComponent(int idx);

    void pickUpEventCardDeck(int deckIdx);

    void putDownEventCardDeck();

    void destroyComponent(int yComponent, int xComponent);

    void readyPlayer();

    void resetTimer();

    void rollDice();

    void close();
}
