package org.progetto.client.connection;

public interface Sender {
    void connect(String serverIp, int serverPort);

    void updateGameList();

    void createGame(int levelGame, int numMaxPlayers);

    void tryJoinToGame(int idGame);

    void showHandComponent();

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

    void destroyComponent(int yComponent, int xComponent);

    void readyPlayer();

    void resetTimer();

    void rollDice();

    void showSpaceship(String owner);

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

    void responsePlanetLandRequest(String response, int idx);

    void responseRewardBox(int idxBox, int xBoxStorage, int yBoxStorage, int idx);

    void responseUseDoubleCannonRequest(String response);
}
