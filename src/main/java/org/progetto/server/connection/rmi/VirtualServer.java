package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualView;
import org.progetto.messages.toClient.GameInfoMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {
    void connect(VirtualView view) throws RemoteException;
    void createGame(VirtualView view, String name, int gameLevel, int numPlayers) throws RemoteException;
    void joinGame(VirtualView view, int idGame, String name) throws RemoteException;
    void startGame(VirtualView view, int idGame) throws RemoteException;
    void pickHiddenComponent(VirtualView view, int idGame, String name) throws RemoteException;
    void pickVisibleComponent(VirtualView view, int idGame, String name, int idx) throws RemoteException;
    void placeHandComponentAndPickHiddenComponent(VirtualView view, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent) throws RemoteException;
    void placeHandComponentAndPickVisibleComponent(VirtualView view, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int idx) throws RemoteException;
    void discardComponent(VirtualView view,int idGame,String name) throws RemoteException;
}