package jemu.rest.dto;

import jemu.system.vz.VZTapeDevice;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class TapeInfo {
    private String name;
    private int position;
    private int positionCount;
    private VZTapeDevice.Mode mode;

    public TapeInfo(String name, int position, int positionCount, VZTapeDevice.Mode mode) {
        this.name = name;
        this.position = position;
        this.positionCount = positionCount;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public VZTapeDevice.Mode getMode() {
        return mode;
    }
}
