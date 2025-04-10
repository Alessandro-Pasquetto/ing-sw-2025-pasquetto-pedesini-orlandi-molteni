package org.progetto.client.connection;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.model.components.Box;

import java.rmi.RemoteException;

import org.progetto.server.model.Player;

public interface Sender {
    void connect(String serverIp, int serverPort);

    void createGame();

    void tryJoinToGame(int idGame);

    void pickHiddenComponent();

    void pickVisibleComponent(int idx);

    void placeLastComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeHandComponentAndPickHiddenComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void placeHandComponentAndPickVisibleComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int componentIdx);

    void placeHandComponentAndPickUpEventCardDeck(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx);

    void placeHandComponentAndPickBookedComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx);

    void placeHandComponentAndReady(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent);

    void discardComponent();

    void bookComponent(int idx);

    void pickBookedComponent(int idx);

    void pickUpEventCardDeck(int deckIdx);

    void putDownEventCardDeck();

    void destroyComponent(int yComponent, int xComponent);

    void readyPlayer();

    void resetTimer();

    void rollDice();

    void showSpaceship(String owner);

    void close();

    void ResponseHowManyDoubleCannons(int howManyWantToUse);

    void ResponseHowManyDoubleEngines(int howManyWantToUse);

    void ResponseBatteryToDiscard(int xBatteryStorage, int yBatteryStorage);

    void ResponseCrewToDiscard(int xHousingUnit, int yHousingUnit);

    void ResponseBoxToDiscard(int xBoxStorage, int yBoxStorage, int idx);

    void ResponseChooseToUseShield(String response);

    void ResponseAcceptRewardCreditsAndPenalties(String response);

    void ResponseLandRequest(String response);

    void ResponseAcceptRewardCreditsAndPenaltyDays(String response);

    void ResponsePlanetLandRequest(String response, int idx);

    void ResponseRewardBox(Box box, int xBoxStorage, int yBoxStorage, int idx);

    void ResponseUseDoubleCannonRequest(String response, int idx);
}
