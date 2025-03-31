package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {

    @Test
    void getType() {
        EventCard eventCard = new TestEventCard(CardType.BATTLEZONE, 2, "img");

        assertEquals(CardType.BATTLEZONE, eventCard.getType());
    }

    @Test
    void getImgSrc() {
        EventCard eventCard = new TestEventCard(CardType.BATTLEZONE, 2, "img");

        assertEquals("img", eventCard.getImgSrc());
    }

    @Test
    void getLevel() {
        EventCard eventCard = new TestEventCard(CardType.BATTLEZONE, 2, "img");

        assertEquals(2, eventCard.getLevel());
    }

    @Test
    void setType() {
        EventCard eventCard = new TestEventCard(CardType.BATTLEZONE, 2, "img");
        eventCard.setType(CardType.LOSTSTATION);

        assertEquals(CardType.LOSTSTATION, eventCard.getType());
    }

    @Test
    void setImgSrc() {
        EventCard eventCard = new TestEventCard(CardType.BATTLEZONE, 2, "img");
        eventCard.setImgSrc("img2");

        assertEquals("img2", eventCard.getImgSrc());
    }
}