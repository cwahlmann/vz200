package jemu.system.vz;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

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
