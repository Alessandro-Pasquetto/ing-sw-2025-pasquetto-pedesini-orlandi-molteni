package org.progetto.client.connection.rmi;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.server.connection.rmi.VirtualServer;
import org.progetto.server.model.Player;
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
            if(GameData.getUIType().equals("GUI"))
                PageController.switchScene("chooseGame.fxml", "ChooseGame");

        } catch (Exception e) {
            System.out.println("Error connecting to the RMI server");
        }
    }

    // The following methods call functions implemented in the RmiServerReceiver

    @Override
    public void createGame() {
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
    public void pickVisibleComponent(int idx){
        try {
            server.pickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), idx);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeLastComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) {
        try {
            server.placeLastComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent){
        try {
            server.placeHandComponentAndPickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent){
        try {
            server.placeHandComponentAndPickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent, idxVisibleComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickUpEventCardDeck(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx){
        try {
            server.placeHandComponentAndPickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent, deckIdx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void placeHandComponentAndPickBookedComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxBookedComponent){
        try {
            server.placeHandComponentAndPickBookedComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent, idxBookedComponent);
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
     * @param xComponent
     * @param yComponent
     */
    @Override
    public void destroyComponent(int xComponent, int yComponent){
        try {
            server.destroyComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xComponent, yComponent);
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
        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showSpaceship(String owner){
        try{
            server.showSpaceship(RmiClientReceiver.getInstance(), GameData.getIdGame(),owner);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        server = null;
        System.out.println("You have disconnected!");

        try {
            if(GameData.getUIType().equals("GUI"))
                PageController.switchScene("connection.fxml", "Page1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseHowManyDoubleCannons(int howManyWantToUse) {
        try {
            server.responseHowManyDoubleCannons(RmiClientReceiver.getInstance(), GameData.getIdGame(), howManyWantToUse);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseHowManyDoubleEngines(int howManyWantToUse) {
        try {
            server.responseHowManyDoubleEngines(RmiClientReceiver.getInstance(), GameData.getIdGame(), howManyWantToUse);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseBatteryToDiscard(int xBatteryStorage, int yBatteryStorage) {
        try {
            server.responseBatteryToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBatteryStorage, yBatteryStorage);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseCrewToDiscard(int xHousingUnit, int yHousingUnit) {
        try {
            server.responseCrewToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xHousingUnit, yHousingUnit);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseBoxToDiscard(int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.responseBoxToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseChooseToUseShield(String response) {
        try {
            server.responseChooseToUseShield(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseAcceptRewardCreditsAndPenalties(String response) {
        try {
            server.responseAcceptRewardCreditsAndPenalties(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseLandRequest(String response) {
        try {
            server.responseLandRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseAcceptRewardCreditsAndPenaltyDays(String response) {
        try {
            server.responseAcceptRewardCreditsAndPenaltyDays(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responsePlanetLandRequest(String response, int idx) {
        try {
            server.responsePlanetLandRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseRewardBox(int idxBox, int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.responseRewardBox(RmiClientReceiver.getInstance(), GameData.getIdGame(), idxBox, xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseUseDoubleCannonRequest(String response) {
        try {
            server.responseUseDoubleCannonRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}