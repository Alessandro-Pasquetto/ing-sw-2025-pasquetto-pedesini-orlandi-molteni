package org.progetto.messages.toClient;

public class AnotherPlayerBookedComponentMessage {

    private String namePlayer;
    private int bookedIndex;
    private String imgSrcBookedComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerBookedComponentMessage(String namePlayer,String imgSrcBookedComponent, int bookedIndex) {
        this.namePlayer = namePlayer;
        this.imgSrcBookedComponent = this.imgSrcBookedComponent;
        this.bookedIndex = bookedIndex;
    }

    // =======================
    // GETTERS
    // =======================

    public String getNamePlayer() {
        return namePlayer;
    }
    public String getImgSrcBookedComponent() {
        return imgSrcBookedComponent;
    }
    public int getBookedIndex() {
        return bookedIndex;
    }



}