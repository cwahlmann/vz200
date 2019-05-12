package jemu.util.assembler.z80;

import java.util.HashMap;
import java.util.Map;

public class AsciiMapper {
	public static final String VZ_CODEC = "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^° !\"#$%&'()*+,-./0123456789:;<=>?";
	private Map<Character, Integer> mappingTable;

	public AsciiMapper() {
		mappingTable = new HashMap<>();
		for (int i=0; i<VZ_CODEC.length(); i++) {
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
