package jemu.system.vz;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class VzTape {
    private List<VzTapeSlot> slots;
    private int position;

    public VzTape() {
        slots = new ArrayList<>();
    }

    @JsonIgnore
    public VzTapeSlot getSlot() {
        while (position >= slots.size()) {
            slots.add(new VzTapeSlot());
        }
        return slots.get(position);
    }

    public int getPosition() {
        return position;
    }

    @JsonIgnore
    public int getSize() {
        return slots.size();
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @JsonIgnore
    public void nextPosition() {
        this.position++;
    }

    public List<VzTapeSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<VzTapeSlot> slots) {
        this.slots = slots;
    }
}
