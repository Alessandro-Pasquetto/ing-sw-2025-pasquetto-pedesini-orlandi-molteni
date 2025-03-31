package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualClient;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to make model's methods visible from RMI client
 */
public interface VirtualServer extends Remote {
    void connect(VirtualClient virtualClient) throws RemoteException;
    void createGame(VirtualClient virtualClient, String name, int gameLevel, int numPlayers) throws RemoteException;
    void joinGame(VirtualClient virtualClient, int idGame, String name) throws RemoteException;
    void startGame(VirtualClient virtualClient, int idGame) throws RemoteException;
    void pickHiddenComponent(VirtualClient virtualClient, int idGame, String name) throws RemoteException;
    void pickVisibleComponent(VirtualClient virtualClient, int idGame, String name, int idx) throws RemoteException;
    void placeHandComponentAndPickHiddenComponent(VirtualClient virtualClient, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent) throws RemoteException;
    void placeHandComponentAndPickVisibleComponent(VirtualClient virtualClient, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx) throws RemoteException;
    void placeHandComponentAndPickUpEventCardDeck(VirtualClient virtualClient, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx) throws RemoteException;
    void discardComponent(VirtualClient virtualClient,int idGame,String name) throws RemoteException;
    void bookComponent(VirtualClient virtualClient,int idGame, String name, int idx) throws RemoteException;
    void pickUpEventCardDeck(VirtualClient virtualClient, int idGame, String name, int deckIdx) throws RemoteException;
    void putDownEventCardDeck(VirtualClient virtualClient, int idGame, String name) throws RemoteException;
    void destroyComponent(VirtualClient virtualClient,int idGame, String game,int yComponent, int xComponent) throws RemoteException;
}