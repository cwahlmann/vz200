package jemu.system.vz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class VzTapeSlot {
    private static final Logger log = LoggerFactory.getLogger(VzTapeSlot.class);

    private byte[] data;
    private int pos;

    public VzTapeSlot() {
        this.data = new byte[]{};
        clear();
    }

    @JsonIgnore
    public Pair<Integer, Integer> read() {
        if (pos < size()) {
            Pair<Integer, Integer> r = Pair.of(get(pos * 4), get(pos * 4 + 2));
            pos++;
            return r;
        }
        return null;
    }

    private int get(int i) {
        return (data[i] & 0xff) * 256 + (data[i + 1] & 0xff);
    }

    @JsonIgnore
    public boolean endOfTape() {
        return pos >= size();
    }

    @JsonIgnore
    public void write(int count, int level) {
        data = ArrayUtils.addAll(data,
                                 (byte) ((count >> 8) & 0xff),
                                 (byte) (count & 0xff),
                                 (byte) ((level >> 8) & 0xff),
                                 (byte) (level & 0xff)
        );
    }

    @JsonIgnore
    public int getPos() {
        return pos;
    }

    @JsonIgnore
    public void setPos(int pos) {
        this.pos = pos;
    }

    @JsonIgnore
    public int size() {
        return data.length / 4;
    }

    @JsonIgnore
    public void clear() {
        this.data = new byte[]{};
        this.pos = 0;
    }

    public String getData() {
        return Base64.getEncoder().encodeToString(data);
    }

    public void setData(String base64) throws IOException {
        pos = 0;
        data = Base64.getDecoder().decode(base64);
    }
}
