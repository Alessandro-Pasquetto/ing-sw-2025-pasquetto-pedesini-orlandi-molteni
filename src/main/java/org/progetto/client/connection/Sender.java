package org.progetto.client.connection;

import java.io.IOException;
import java.rmi.NotBoundException;

public interface Sender {
    void connect(String serverIp, int serverPort) throws IOException, NotBoundException;

    void updateGameList();

    void createGame(int levelGame, int numMaxPlayers);

    void tryJoinToGame(int idGame);

    void showHandComponent();

    void buildShip(int idShip);

    void pickHiddenComponent();

    void showVisibleComponents();

    void pickVisibleComponent(int idx);

    void placeComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeLastComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeHandComponentAndPickHiddenComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeHandComponentAndPickVisibleComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int componentIdx);

    void placeHandComponentAndPickUpEventCardDeck(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx);

    void placeHandComponentAndPickBookedComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx);

    void placeHandComponentAndReady(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void discardComponent();

    void bookComponent(int idx);

    void showBookedComponents();

    void pickBookedComponent(int idx);

    void pickUpEventCardDeck(int deckIdx);

    void putDownEventCardDeck();

    void destroyComponent(int xComponent, int yComponent);

    void readyPlayer();

    void resetTimer();

    void playerStats();

    void showSpaceship(String owner);

    void spaceshipStats();

    void showTrack();

    void close();

    void responseHowManyDoubleCannons(int howManyWantToUse);

    void responseHowManyDoubleEngines(int howManyWantToUse);

    void responseBatteryToDiscard(int xBatteryStorage, int yBatteryStorage);

    void responseCrewToDiscard(int xHousingUnit, int yHousingUnit);

    void responseBoxToDiscard(int xBoxStorage, int yBoxStorage, int idx);

    void responseChooseToUseShield(String response);

    void responseAcceptRewardCreditsAndPenalties(String response);

    void responseLandRequest(String response);

    void responseAcceptRewardCreditsAndPenaltyDays(String response);

    void responsePlanetLandRequest(int idx);

    void responseRewardBox(int idxBox, int xBoxStorage, int yBoxStorage, int idx);

    void responseUseDoubleCannonRequest(String response);

    void responseContinueTravel(String response);

    void responseRollDice();

    void responseSelectSpaceshipPart(int x, int y);
}
