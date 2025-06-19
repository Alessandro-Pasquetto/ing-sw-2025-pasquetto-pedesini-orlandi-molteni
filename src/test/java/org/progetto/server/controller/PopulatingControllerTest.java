package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.messages.toClient.Populating.AlienPlacedMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PopulatingControllerTest {

    @Test
    void populatingController() {
        //setup
        GameManager gameManager = new GameManager(0, 4, 1);
        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().getBoard().addTraveler(player);
        gameManager.getGame().setPhase(GamePhase.POPULATING);

        List<Object> sentMessages = new ArrayList<>();

        Sender sender = sentMessages::add;
        gameManager.addSender(player, sender);

        // Aggiungiamo componenti per abilitare alieno viola
        Spaceship spaceship = player.getSpaceship();
        BuildingBoard board = spaceship.getBuildingBoard();

        board.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3,3,3,3}, "img", 2));
        board.placeComponent(2, 1, 0);

        board.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3,3,3,3}, "img"));
        board.placeComponent(1, 1, 0);

        board.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3,3,3,3}, "img", 2));
        board.placeComponent(1, 2, 0);

        board.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3,3,3,3}, "img", 2));
        board.placeComponent(2, 3, 0);

        board.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3,3,3,3}, "img"));
        board.placeComponent(1, 3, 0);

        board.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3,3,3,3}, "img", 2));
        board.placeComponent(3, 3, 0);

        board.initSpaceshipParams();

        // alien request
        PopulatingController.askAliensToSinglePlayer(gameManager, player);

        // verify purple alien is requested
        assertEquals(1, sentMessages.size());
        Object msg = sentMessages.getFirst();
        assertInstanceOf(AskAlienMessage.class, msg);
        assertEquals("purple", ((AskAlienMessage) msg).getColor());

        // purple alien
        PopulatingController.receivePlaceAlien(gameManager, player, 2, 1, "purple");

        // orange alien
        PopulatingController.receivePlaceAlien(gameManager, player, 2, 3, "orange");
    }
}