package jemu.util.assembler.z80;

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

public class AsciiMapper {
	public static final char BACKSPACE = (char) 0x09;
	public static final String VZ_CODEC = "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^" + BACKSPACE
			+ " !\"#$%&'()*+,-./0123456789:;<=>?";
	private Map<Character, Integer> mappingTable;

	public AsciiMapper() {
		mappingTable = new HashMap<>();
		for (int i = 0; i < VZ_CODEC.length(); i++) {
			mappingTable.put(VZ_CODEC.charAt(i), i);
		}
	}

	public Integer map(Character ascii) {
		Character ASCII = Character.toUpperCase(ascii);
		if (mappingTable.containsKey(ASCII)) {
			return mappingTable.get(ASCII);
		}
		return 0;
	}
}
