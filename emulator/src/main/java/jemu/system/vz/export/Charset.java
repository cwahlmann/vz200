package jemu.system.vz.export;

import com.google.common.primitives.Chars;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@Component
public class Charset {
    // 20-3f = 40-5f
    private static final String ASC_40_7F = "@ABCDEFGHIJKLMNO" + // 40
                                            "PQRSTUVWXYZ[\\]^~" + // 50
                                            " !\"#$%&'()*+,-./" + // 60
                                            "0123456789:;<=>?"; // 70
    private static final int ASC_20_OFFSET = 0x20;
    private static final int ASC_40_OFFSET = 0x40;

    // c0-ff = 40-5f invers: '|c' = invers
    private static final int INVERS_OFFSET = 0xc0;
    public static final String INVERS_PREF = "|";

    // 00-1f, 80-bf: '_hh'
    public static final String ASC_PREF = "_";
    private static final int GFX_OFFSET = 0x80;

    private static Map<Character, Integer> stringToAscMap = new HashMap<>();
    private static Map<Integer, Character> ascToStringMap = new HashMap<>();

    static {
        for (int i = 0x40; i < 0x80; i++) {
            stringToAscMap.put(ASC_40_7F.charAt(i - 0x40), i);
            ascToStringMap.put(i, ASC_40_7F.charAt(i - 0x40));
        }
    }

    public static String ascToString(int b) {
        int asc = b < 0 ? b + 0xff : b;
        if ((asc >= GFX_OFFSET && asc < INVERS_OFFSET) || (asc < ASC_20_OFFSET)) {
            return String.format(ASC_PREF + "%02X", asc);
        }
        if (asc >= INVERS_OFFSET) {
            return INVERS_PREF + ascToStringMap.get(asc - INVERS_OFFSET + ASC_40_OFFSET);
        }
        if (asc < ASC_40_OFFSET) {
            asc = asc + 0x40;
        }
        return "" + ascToStringMap.get(asc);
    }

    public static int stringToAsc(String s) {
        if (StringUtils.isEmpty(s)) {
            return 0;
        }
        if (s.startsWith(ASC_PREF)) {
            if (s.length() != 3) {
                return 0;
            }
            return Integer.valueOf(s.substring(1), 16);
        }
        if (s.startsWith(INVERS_PREF)) {
            if (s.length() != 2) {
                return 0;
            }
            return stringToAscMap.getOrDefault(s.charAt(1), 0) - ASC_40_OFFSET + INVERS_OFFSET;
        }
        int c = stringToAscMap.getOrDefault(s.charAt(0), 0);
        if (c >= 0x60) {
            return c - 0x40;
        }
        return c;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 0x100; i++) {
            String s = Charset.ascToString(i);
            System.out.println(String.format("%02X: %s => %02X", i, s, Charset.stringToAsc(s)));
        }
    }
}
