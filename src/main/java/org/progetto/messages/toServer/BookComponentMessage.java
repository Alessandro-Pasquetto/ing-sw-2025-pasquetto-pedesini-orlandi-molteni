package org.progetto.messages.toServer;

public class BookComponentMessage {

    // =======================
    // ATTRIBUTES
    // =======================

    int bookIdx;

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