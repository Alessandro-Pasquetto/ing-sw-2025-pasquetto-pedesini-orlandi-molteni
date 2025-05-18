package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.RmiClientReceiver;
import org.progetto.client.connection.rmi.VirtualClient;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to make model's methods visible from RMI client
 */
public interface VirtualServer extends Remote {

    void connect(VirtualClient virtualClient) throws RemoteException;

    void ping() throws RemoteException;

    void showWaitingGames(VirtualClient virtualClient) throws RemoteException;

    void createGame(VirtualClient virtualClient, String name, int gameLevel, int numPlayers) throws RemoteException;

    void joinGame(VirtualClient virtualClient, int idGame, String name) throws RemoteException;

    void reconnectToGame(VirtualClient virtualClient, int idGame, String name) throws RemoteException;

    void pickHiddenComponent(VirtualClient virtualClient, int idGame) throws RemoteException;

    void pickVisibleComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException;

    void placeComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException;

    void buildShip(VirtualClient virtualClient, int idGame, int idShip) throws RemoteException;

    void placeLastComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException;

    void placeHandComponentAndPickHiddenComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException;

    void placeHandComponentAndPickVisibleComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent) throws RemoteException;

    void placeHandComponentAndPickUpEventCardDeck(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx) throws RemoteException;

    void placeHandComponentAndPickBookedComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx) throws RemoteException;

    void placeHandComponentAndReady(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException;

    void discardComponent(VirtualClient virtualClient, int idGame) throws RemoteException;

    void bookComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException;

    void pickBookedComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException;

    void pickUpEventCardDeck(VirtualClient virtualClient, int idGame, int deckIdx) throws RemoteException;

    void putDownEventCardDeck(VirtualClient virtualClient, int idGame) throws RemoteException;

    void destroyComponent(VirtualClient virtualClient, int idGame, int xComponent, int yComponent) throws RemoteException;

    void playerReady(VirtualClient virtualClient, int idGame) throws RemoteException;

    void resetTimer(VirtualClient virtualClient, int idGame) throws RemoteException;

    void playerStats(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showPlayers(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showStartingPositions(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showPlayersInPositioningDecisionOrder(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showSpaceship(VirtualClient virtualClient, int idGame, String owner) throws RemoteException;

    void spaceshipStats(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showTrack(VirtualClient virtualClient, int idGame) throws RemoteException;

    void rollDice(VirtualClient virtualClient, int idGame) throws RemoteException, InterruptedException;

    void showVisibleComponents(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showBookedComponents(VirtualClient virtualClient, int idGame) throws RemoteException;

    void showHandComponent(VirtualClient virtualClient, int idGame) throws RemoteException;

    void responsePlaceAlien(VirtualClient virtualClient, int idGame, int x, int y, String color) throws RemoteException;

    void responseStartingPosition(VirtualClient virtualClient, int idGame, int startingPosition) throws RemoteException;

    void responseHowManyDoubleCannons(VirtualClient virtualClient, int idGame, int howManyWantToUse) throws RemoteException;

    void responseHowManyDoubleEngines(VirtualClient virtualClient, int idGame, int howManyWantToUse) throws RemoteException;

    void responseBatteryToDiscard(VirtualClient virtualClient, int idGame, int xBatteryStorage, int yBatteryStorage) throws RemoteException;

    void responseCrewToDiscard(VirtualClient virtualClient, int idGame, int xHousingUnit, int yHousingUnit) throws RemoteException;

    void responseBoxToDiscard(VirtualClient virtualClient, int idGame, int xBoxStorage, int yBoxStorage, int idx) throws RemoteException;

    void responseChooseToUseShield(VirtualClient virtualClient, int idGame, String response) throws RemoteException;

    void responseUseDoubleCannonRequest(VirtualClient virtualClient, int idGame, String response) throws RemoteException;

    void responseAcceptRewardCreditsAndPenalties(VirtualClient virtualClient, int idGame, String response) throws RemoteException;

    void responseLandRequest(VirtualClient virtualClient, int idGame, String response) throws RemoteException;

    void responseAcceptRewardCreditsAndPenaltyDays(VirtualClient virtualClient, int idGame, String response) throws RemoteException;

    void responsePlanetLandRequest(VirtualClient virtualClient, int idGame, int idx) throws RemoteException;

    void responseRewardBox(VirtualClient virtualClient, int idGame, int idxBox, int xBoxStorage, int yBoxStorage, int idx) throws RemoteException;

    void responseContinueTravel(VirtualClient virtualClient, int idGame, String response) throws RemoteException;

    void responseRollDice(VirtualClient virtualClient, int idGame) throws RemoteException;

    void responseSelectSpaceshipPart(VirtualClient virtualClient, int idGame, int x, int y) throws RemoteException;
}