package org.progetto.client.connection.rmi;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.server.connection.rmi.VirtualServer;
import org.progetto.server.model.components.Box;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class RmiClientSender implements Sender {

    private VirtualServer server = null;

    public RmiClientSender() {}

    /**
     * Method to connect to the RMI server
     */
    @Override
    public void connect(String serverIp, int serverPort) {
        try {
            server = (VirtualServer) Naming.lookup("//" + serverIp + ":" + serverPort + "/VirtualServer");

            server.connect(RmiClientReceiver.getInstance());

            System.out.println("Connected to the RMIServer");
            PageController.switchScene("chooseGame.fxml", "ChooseGame");

        } catch (Exception e) {
            System.out.println("Error connecting to the RMI server");
        }
    }

    // The following methods call functions implemented in the RmiServerReceiver

    @Override
    public void createGame() {
        System.out.println("You have created a new game");
        try{
            server.createGame(RmiClientReceiver.getInstance(), GameData.getNamePlayer(), 1, 4);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tryJoinToGame(int idGame) {
        try {
            server.joinGame(RmiClientReceiver.getInstance(), idGame, GameData.getNamePlayer());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pickHiddenComponent(){
        try {
            server.pickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pickVisibleComponent(){
        try {
            server.pickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), -1); // non so dove pescare l'idx :C
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeLastComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent) {
        try {
            server.placeLastComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent){
        try {
            server.placeHandComponentAndPickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int idxVisibleComponent){
        try {
            server.placeHandComponentAndPickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent, idxVisibleComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickUpEventCardDeck(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx){
        try {
            server.placeHandComponentAndPickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent, deckIdx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickBookedComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxBookedComponent){
        try {
            server.placeHandComponentAndPickBookedComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent, idxBookedComponent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndReady(int xHandComponent, int yHandComponent, int rHandComponent) {
        try {
            server.placeHandComponentAndReady(RmiClientReceiver.getInstance(), GameData.getIdGame(), xHandComponent, yHandComponent, rHandComponent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows RMI on client to call for the respective method on RMI server
     *
     * @author Lorenzo
     */
    @Override
    public void discardComponent(){
        try {
            server.discardComponent(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows RMI on client to call for the respective method on RMI server
     *
     * @author Lorenzo
     * @param idx is the index were the component will be placed
     */
    @Override
    public void bookComponent(int idx){
        try {
            server.bookComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), idx);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows RMI on client to call for the respective method on RMI server
     *
     * @author Lorenzo
     * @param idx is the index were the component will be picked
     */
    @Override
    public void pickBookedComponent(int idx){
        try {
            server.pickBookedComponent(RmiClientReceiver.getInstance(),GameData.getIdGame(),idx);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pickUpEventCardDeck(int deckIdx) {
        try {
            server.pickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), deckIdx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putDownEventCardDeck() {
        try {
            server.putDownEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows client to call for the destroyComponent method on RMI server
     *
     * @author Lorenzo
     * @param yComponent
     * @param xComponent
     */
    @Override
    public void destroyComponent(int yComponent, int xComponent){
        try {
            server.destroyComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yComponent,xComponent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readyPlayer() {
        try {
            server.playerReady(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resetTimer() {
        try {
            server.resetTimer(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollDice() {
        try {
            server.rollDice(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        server = null;
        System.out.println("You have disconnected!");
        try {
            PageController.switchScene("connection.fxml", "Page1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseHowManyDoubleCannons(int howManyWantToUse) {
        try {
            server.responseHowManyDoubleCannons(RmiClientReceiver.getInstance(), GameData.getIdGame(), howManyWantToUse);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseHowManyDoubleEngines(int howManyWantToUse) {
        try {
            server.responseHowManyDoubleEngines(RmiClientReceiver.getInstance(), GameData.getIdGame(), howManyWantToUse);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseBatteryToDiscard(int xBatteryStorage, int yBatteryStorage) {
        try {
            server.responseBatteryToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBatteryStorage, yBatteryStorage);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseCrewToDiscard(int xHousingUnit, int yHousingUnit) {
        try {
            server.responseCrewToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xHousingUnit, yHousingUnit);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseBoxToDiscard(int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.responseBoxToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseChooseToUseShield(String response) {
        try {
            server.responseChooseToUseShield(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseAcceptRewardCreditsAndPenalties(String response) {
        try {
            server.responseAcceptRewardCreditsAndPenalties(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseLandRequest(String response) {
        try {
            server.responseLandRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseAcceptRewardCreditsAndPenaltyDays(String response) {
        try {
            server.responseAcceptRewardCreditsAndPenaltyDays(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponsePlanetLandRequest(String response, int idx) {
        try {
            server.responsePlanetLandRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseRewardBox(Box box, int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.responseRewardBox(RmiClientReceiver.getInstance(), GameData.getIdGame(), box, xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseUseDoubleCannonRequest(String response, int idx) {
        try {
            server.responseUseDoubleCannonRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}