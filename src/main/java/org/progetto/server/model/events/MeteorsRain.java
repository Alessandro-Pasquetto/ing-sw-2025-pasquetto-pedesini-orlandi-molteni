package org.progetto.server.model.events;
import java.util.ArrayList;

public class MeteorsRain extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<Projectile> meteors;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRain(CardType type, String imgSrc, ArrayList<Projectile> meteors) {
        super(type, imgSrc);
        this.meteors = meteors;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // For each meteorite, the leader rolls two dice
    // The sum of the two dice determines which row or column the meteorite will hit
    // Each player is hit by a personal meteorite and checks whether it hits or misses their ship
    // A small meteorite will bounce harmlessly off a well-assembled ship. It is only a problem if it hits an exposed connector
    // In this case, you can still avoid the damage by deploying a shield, if you have one protecting that side
    // You must spend 1 battery token to do so
    // If you cannot or do not want to avoid the impact, the component hit by the meteorite is destroyed
    // Remove it from the ship and add it to your pile of components lost along the way
    // A large meteorite will damage even a well-assembled ship and shields cannot stop it
    // Your only hope is to make it explode
    // You can only shoot at it if you have a cannon pointed at it in the same column
    // If it is a double cannon, you must pay 1 battery token to activate it
    public void effect() {

    }
}
