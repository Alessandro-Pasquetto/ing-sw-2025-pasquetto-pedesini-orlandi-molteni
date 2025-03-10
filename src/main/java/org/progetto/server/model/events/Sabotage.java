package org.progetto.server.model.events;
import java.util.List;

public class Sabotage extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Sabotage(CardType type, String imgSrc) {
        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Sabotage destroys a random component of the ship with the least crew (among several ships tied for the least crew, only the one furthest ahead on the route is sabotaged)
    // To select the component, the damaged player rolls 2 dice to determine the column and then rolls the dices again to determine the row
    // If there are no components at those coordinates, he rolls the dice twice more to establish new ones
    // If there are still no components, he rolls again
    // If after three double rolls for the coordinates no component has been hit, the saboteurs surrender and nothing happens
    public void effect() {

    }
}
