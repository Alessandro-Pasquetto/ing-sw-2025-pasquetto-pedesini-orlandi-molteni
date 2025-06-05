package org.progetto.messages.toServer;

import java.io.Serializable;

public class BookComponentMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int bookIdx;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BookComponentMessage(int bookIdx) {
        this.bookIdx = bookIdx;
    }

    // =======================
    // GETTERS
    // =======================

    public int getBookIdx() {
        return bookIdx;
    }


}