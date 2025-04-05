package org.progetto.server.controller.events;

import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.Projectile;

import java.util.ArrayList;

public class PiratesController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private GameManager gameManager;
    private String phase;
    private int currPlayer;
    private ArrayList<Player> activePlayers;
    private float playerFirePower;
    private int requestedBatteries;
    private ArrayList<Player> defeatedPlayers;
    private ArrayList<Projectile> penaltyShots;
    private int responsesCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PiratesController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.phase = "START";
        this.currPlayer = 0;
        this.activePlayers = gameManager.getGame().getBoard().getActivePlayers();
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.requestedCrew = 0;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // WIP...
}
