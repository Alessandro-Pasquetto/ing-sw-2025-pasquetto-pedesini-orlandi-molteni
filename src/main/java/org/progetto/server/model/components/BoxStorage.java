package org.progetto.server.model.components;

import org.progetto.server.model.Spaceship;

public class BoxStorage extends Component {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Box[] boxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BoxStorage(ComponentType type, int[] connections, String imgSrc, int capacity) {
        super(type, connections, imgSrc);
        this.boxes = new Box[capacity];
    }

    // =======================
    // GETTERS
    // =======================

    public int getCapacity() {
        return boxes.length;
    }

    public Box[] getBoxes() {
        return boxes;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Adds the given box to boxStorage at the given index idx
     *
     * @author Gabriele
     * @param box Box to add
     * @param idx Storage index where to add it
     */
    public void addBox(Spaceship spaceship, Box box, int idx) throws IllegalStateException{

        if(box == null)
            throw new IllegalStateException("NullBox");

        if(idx < 0 || idx >= boxes.length)
            throw new IllegalStateException("InvalidBoxIdx");

        if(boxes[idx] != null)
            throw new IllegalStateException("FullBoxSlot");

        if(box == Box.RED && !type.equals(ComponentType.RED_BOX_STORAGE))
            throw new IllegalStateException("CantStoreInANonRedStorage");

        boxes[idx] = box;
        spaceship.addBoxCount(1, box);
    }

    public void tryToAddBox(Box box, int idx) throws IllegalStateException{

        if(box == null)
            throw new IllegalStateException("NullBox");

        if(idx < 0 || idx >= boxes.length)
            throw new IllegalStateException("InvalidBoxIdx");

        if(boxes[idx] != null)
            throw new IllegalStateException("FullBoxSlot");

        if(box == Box.RED && !type.equals(ComponentType.RED_BOX_STORAGE))
            throw new IllegalStateException("CantStoreInANonRedStorage");
    }

    /**
     * Remove the box from boxStorage at the given index idx
     *
     * @author Gabriele
     * @param idx Storage index to remove
     */
    public void removeBox(Spaceship spaceship, int idx) throws IllegalStateException {

        if(idx < 0 || idx >= boxes.length)
            throw new IllegalStateException("InvalidBoxIdx");

        if(boxes[idx] == null)
            throw new IllegalStateException("EmptyBoxSlot");

        spaceship.addBoxCount(-1, boxes[idx]);
        boxes[idx] = null;
    }
}
