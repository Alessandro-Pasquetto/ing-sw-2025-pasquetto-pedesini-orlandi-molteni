package org.progetto.client.connection.rmi;

import javafx.application.Platform;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.server.connection.rmi.VirtualServer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RmiClientSender implements Sender {

    private VirtualServer server = null;

    private static int rmiServerDisconnectionDetectionInterval;

    public RmiClientSender() {}

    public static void setRmiServerDisconnectionDetectionInterval(int rmiServerDisconnectionDetectionInterval) {
        RmiClientSender.rmiServerDisconnectionDetectionInterval = rmiServerDisconnectionDetectionInterval;
    }

    /**
     * Method to connect to the RMI server
     */
    @Override
    public void connect(String serverIp, int serverPort) throws MalformedURLException, NotBoundException, RemoteException {
        server = (VirtualServer) Naming.lookup("//" + serverIp + ":" + serverPort + "/VirtualServer");

        server.connect(RmiClientReceiver.getInstance());

        startRmiServerPinger();

        System.out.println("Connected to the RMIServer");
    }

    private void startRmiServerPinger(){
        Thread pingThread = new Thread(() -> {
            while (true) {
                try{
                    server.ping();
                }catch (RemoteException e){
                    System.err.println("RMI server unreachable");

                    disconnected();
                    return;
                }

                try {
                    Thread.sleep(rmiServerDisconnectionDetectionInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

    @Override
    public void updateGameList(){
        try{
            server.showWaitingGames(RmiClientReceiver.getInstance());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void createGame(int levelGame, int numMaxPlayers) {
        try{
            server.createGame(RmiClientReceiver.getInstance(), GameData.getNamePlayer(), levelGame, numMaxPlayers);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void tryJoinToGame(int idGame) {
        try {
            server.joinGame(RmiClientReceiver.getInstance(), idGame, GameData.getNamePlayer());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void reconnectToGame(){

        try {
            server.reconnectToGame(RmiClientReceiver.getInstance(), GameData.getIdGame(), GameData.getNamePlayer());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showHandComponent(){
        try {
            server.showHandComponent(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void pickHiddenComponent(){
        try {
            server.pickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showVisibleComponents(){
        try {
            server.showVisibleComponents(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void pickVisibleComponent(int idx){
        try {
            server.pickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), idx);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) {
        try {
            server.placeComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void buildShip(int idShip){
        try{
            server.buildShip(RmiClientReceiver.getInstance(), GameData.getIdGame(), idShip);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeLastComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) {
        try {
            server.placeLastComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent){
        try {
            server.placeHandComponentAndPickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxVisibleComponent){
        try {
            server.placeHandComponentAndPickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent, idxVisibleComponent);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeHandComponentAndPickUpEventCardDeck(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx){
        try {
            server.placeHandComponentAndPickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent, deckIdx);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeHandComponentAndPickBookedComponent(int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idxBookedComponent){
        try {
            server.placeHandComponentAndPickBookedComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), xPlaceComponent, yPlaceComponent, rPlaceComponent, idxBookedComponent);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void placeHandComponentAndReady(int xHandComponent, int yHandComponent, int rHandComponent) {
        try {
            server.placeHandComponentAndReady(RmiClientReceiver.getInstance(), GameData.getIdGame(), xHandComponent, yHandComponent, rHandComponent);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
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
            System.err.println("RMI client unreachable");
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
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showBookedComponents(){
        try {
            server.showBookedComponents(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
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
            server.pickBookedComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), idx);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void pickUpEventCardDeck(int deckIdx) {
        try {
            server.pickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), deckIdx);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void putDownEventCardDeck() {
        try {
            server.putDownEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
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
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void readyPlayer() {
        try {
            server.playerReady(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void resetTimer() {
        try {
            server.resetTimer(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void playerStats(){
        try{
            server.playerStats(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showPlayers(){
        try{
            server.showPlayers(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showStartingPositions(){
        try{
            server.showStartingPositions(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showPlayersInPositioningDecisionOrder() {
        try{
            server.showPlayersInPositioningDecisionOrder(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showSpaceship(String owner){
        try{
            server.showSpaceship(RmiClientReceiver.getInstance(), GameData.getIdGame(), owner);
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void spaceshipStats() {
        try{
            server.spaceshipStats(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void showTrack() {
        try{
            server.showTrack(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responsePlaceAlien(int x, int y, String color) {
        try {
            server.responsePlaceAlien(RmiClientReceiver.getInstance(), GameData.getIdGame(), x, y, color);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseStartingPosition(int startingPosition) {
        try {
            server.responseStartingPosition(RmiClientReceiver.getInstance(), GameData.getIdGame(), startingPosition);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseHowManyDoubleCannons(int howManyWantToUse) {
        try {
            server.responseHowManyDoubleCannons(RmiClientReceiver.getInstance(), GameData.getIdGame(), howManyWantToUse);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseHowManyDoubleEngines(int howManyWantToUse) {
        try {
            server.responseHowManyDoubleEngines(RmiClientReceiver.getInstance(), GameData.getIdGame(), howManyWantToUse);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseBatteryToDiscard(int xBatteryStorage, int yBatteryStorage) {
        try {
            server.responseBatteryToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBatteryStorage, yBatteryStorage);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseCrewToDiscard(int xHousingUnit, int yHousingUnit) {
        try {
            server.responseCrewToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xHousingUnit, yHousingUnit);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseBoxToDiscard(int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.responseBoxToDiscard(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseChooseToUseShield(String response) {
        try {
            server.responseChooseToUseShield(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseAcceptRewardCreditsAndPenalties(String response) {
        try {
            server.responseAcceptRewardCreditsAndPenalties(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseLandRequest(String response) {
        try {
            server.responseLandRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseAcceptRewardCreditsAndPenaltyDays(String response) {
        try {
            server.responseAcceptRewardCreditsAndPenaltyDays(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseAcceptRewardBoxesAndPenaltyDays(String response) {
        try {
            server.responseAcceptRewardBoxesAndPenaltyDays(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responsePlanetLandRequest(int idx) {
        try {
            server.responsePlanetLandRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), idx);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseRewardBox(int rewardIdxBox, int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.responseRewardBox(RmiClientReceiver.getInstance(), GameData.getIdGame(), rewardIdxBox, xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void moveBox(int xStart, int yStart, int idxStart, int xDestination, int yDestination, int idxDestination) {
        try {
            server.moveBox(RmiClientReceiver.getInstance(), GameData.getIdGame(), xStart, yStart, idxStart, xDestination, yDestination, idxDestination);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void removeBox(int xBoxStorage, int yBoxStorage, int idx) {
        try {
            server.removeBox(RmiClientReceiver.getInstance(), GameData.getIdGame(), xBoxStorage, yBoxStorage, idx);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseUseDoubleCannonRequest(String response) {
        try {
            server.responseUseDoubleCannonRequest(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseContinueTravel(String response) {
        try {
            server.responseContinueTravel(RmiClientReceiver.getInstance(), GameData.getIdGame(), response);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseRollDice() {
        try {
            server.responseRollDice(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void responseSelectSpaceshipPart(int x, int y) {
        try {
            server.responseSelectSpaceshipPart(RmiClientReceiver.getInstance(), GameData.getIdGame(), x, y);
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    @Override
    public void leaveGame() {
        try {
            server.leaveGame(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    public void disconnected() {
        server = null;
        System.out.println("You have disconnected!");

        if(GameData.getUIType().equals("GUI")) {
            Platform.runLater(() -> {
                try {
                    PageController.switchScene("connection.fxml", "Connection");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}