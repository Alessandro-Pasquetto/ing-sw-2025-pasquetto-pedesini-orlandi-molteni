package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {

    @Test
    void getType() {
        OpenSpace eventCard = new OpenSpace(CardType.OPENSPACE, 2, "img");

        assertEquals(CardType.OPENSPACE, eventCard.getType());
    }

    @Test
    void getImgSrc() {
        OpenSpace eventCard = new OpenSpace(CardType.OPENSPACE, 2, "img");

        assertEquals("img", eventCard.getImgSrc());
    }

    @Test
    void getLevel() {
        OpenSpace eventCard = new OpenSpace(CardType.OPENSPACE, 2, "img");

        assertEquals(2, eventCard.getLevel());
    }

    @Test
    void setType() {
        OpenSpace eventCard = new OpenSpace(CardType.OPENSPACE, 2, "img");
        eventCard.setType(CardType.LOSTSTATION);

        assertEquals(CardType.LOSTSTATION, eventCard.getType());
    }

    @Test
    void setImgSrc() {
        OpenSpace eventCard = new OpenSpace(CardType.OPENSPACE, 2, "img");
        eventCard.setImgSrc("img2");

        assertEquals("img2", eventCard.getImgSrc());
    }
}