package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.Component;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    // TODO: TEST DA RIVEDERE -> sarebbe meglio testare metodo per metodo!

    @Test
    void GameCreationTest() {
        Game game = new Game(1,4,1);
        Player mario = new Player("mario",0,game.getLevel());
        Player alice = new Player("alice",1,game.getLevel());

        game.addPlayer(mario);
        game.addPlayer(alice);

        Spaceship sp_1 = game.getPlayers().get(0).getSpaceship();            // get spaceship
        BuildingBoard bb_1 = sp_1.getBuildingBoard();                        // get building board
        Component component = game.pickHiddenComponent(mario);               // get fist component
        bb_1.setHandComponent(component);                                    // pick fist component
        bb_1.placeComponent(0,2, 0);                                // place component

        System.out.println("Hidden " + component.isHidden());
        System.out.println("Placed " + component.isPlaced());
        bb_1.printBoard();
    }

    @Test
    void Loadingtest() throws IOException {
        Game game = new Game(0,4,2);



    }

    // ===================

    @BeforeEach
    void setUp() {
    }

    @Test
    void getId() {
    }

    @Test
    void getLevel() {
    }

    @Test
    void getPhase() {
    }

    @Test
    void getPlayers() {
    }

    @Test
    void getPlayersSize() {
    }

    @Test
    void getMaxNumPlayers() {
    }

    @Test
    void getBoard() {
    }

    @Test
    void setPhase() {
    }

    @Test
    void saveGame() {
    }

    @Test
    void endGame() {
    }

    @Test
    void addPlayer() {
    }

    @Test
    void pickHiddenComponent() {
    }

    @Test
    void pickVisibleComponent() {
    }

    @Test
    void pickEventCard() {
    }

    @Test
    void tryAddPlayer() {
    }
}