package jemu.util.assembler.z80;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class LineParserTest {
	private static final Logger log = LoggerFactory.getLogger(LineParser.class);
	private static final List<String> SWAPPED_PARAMETERS = Arrays
			.asList(new String[] { "BIT b,(IX+o)", "BIT b,(IY+o)", "BIT b,r", "LD (IX+o),n", "LD (IY+o),n",
					"RES b,(IX+o)", "RES b,(IY+o)", "RES b,r", "SET b,(IX+o)", "SET b,(IY+o)", "SET b,r" });

	private static final String IGNORE_FOR_TEST_SYMBOL = ".*\\((IX|IY)\\+o\\).*";

	@TestFactory
	public Stream<DynamicTest> parseCommandsTest() {
		return Command.commands.stream()
				.map(command -> DynamicTest.dynamicTest("test command '" + command.getDefinition() + "'", () -> {
					String definition = command.getDefinition();
					Pair<Integer, String> pairA = containsRegister(definition) ? Pair.of(-1, definition)
							: replace(definition);
					Pair<Integer, String> pairB = replace(pairA.getRight());
					definition = pairB.getRight();
					List<Integer> result = LineParser.parseLine(0, definition).stream()
							.map(token -> token.value().orElse(-1)).collect(Collectors.toList());
					Integer a = pairA.getLeft();
					Integer b = pairB.getLeft();
					List<Integer> expected;
					if (SWAPPED_PARAMETERS.contains(command.getDefinition())) {
						expected = command.getOpcode().stream().map(o -> o.apply(b, a)).collect(Collectors.toList());
					} else {
						expected = command.getOpcode().stream().map(o -> o.apply(a, b)).collect(Collectors.toList());
					}
					assertEquals(expected, result);
				}));
	}

	@Test
	public void parseCommandsSymbolTest() {
		parseCommandsSymbolTest("LD A, JOE:", 2, "A", "JOE:");
		parseCommandsSymbolTest("LD (JOE:), A", 3, "(JOE:)", "A");
		parseCommandsSymbolTest("JR JOE:", 2, "JOE:", "");
		parseCommandsSymbolTest("JP JOE:", 3, "JOE:", "");
	}
	
	private void parseCommandsSymbolTest(String line, int size, String sourcaA, String sourceB) {
		List<Token> result = LineParser.parseLine(0, line);
		assertEquals(size, result.size());
		for (int i=0; i<size; i++) {
			assertFalse(result.get(i).value().isPresent());
			assertEquals(sourcaA, result.get(i).sourceA());
			assertEquals(sourceB, result.get(i).sourceB());			
		}
	}

	private boolean containsRegister(String d) {
		for (String r : Constants.R) {
			if (Pattern.matches(".*" + r + "\\s*,.*", d)) {
				return true;
			}
		}
		for (String p : Constants.P) {
			if (Pattern.matches(".*" + p + "\\s*,.*", d)) {
				return true;
			}
		}
		for (String q : Constants.Q) {
			if (Pattern.matches(".*" + q + "\\s*,.*", d)) {
				return true;
			}
		}

		for (String q : new String[] { "M", "NZ", "P", "PO", "Z", "IX", "IY", "\\(\\s*HL\\s*\\)" }) {
			if (Pattern.matches(".*" + q + "\\s*,.*", d)) {
				return true;
			}
		}

		return false;
	}

	private Pair<Integer, String> replace(String d) {
		int index = d.indexOf("nn");
		if (index >= 0) {
			return Pair.of(0x1234, d.replace("nn", "0x1234"));
		}
		index = d.indexOf("n");
		if (index >= 0) {
			return Pair.of(0xef, d.replace("n", "0xef"));
		}
		index = d.indexOf("o");
		if (index >= 0) {
			return Pair.of(35, d.replace("o", "35"));
		}
		index = d.indexOf("r");
		if (index >= 0) {
			return Pair.of(3, d.replace("r", Constants.R[3]));
		}
		index = d.indexOf("p");
		if (index >= 0) {
			return Pair.of(5, d.replace("p", Constants.P[5]));
		}
		index = d.indexOf("q");
		if (index >= 0) {
			return Pair.of(5, d.replace("q", Constants.Q[5]));
		}
		index = d.indexOf("b");
		if (index >= 0) {
			return Pair.of(4, d.replace("b", "4"));
		}
		return Pair.of(0, d);
	}

	private Pair<Integer, String> replace1(String d) {
		int index = d.indexOf("nn");
		if (index >= 0) {
			return Pair.of(-1, d.replace("nn", "test:"));
		}
		index = d.indexOf("n");
		if (index >= 0) {
			return Pair.of(-1, d.replace("n", "test:"));
		}
		index = d.indexOf("o");
		if (index >= 0) {
			return Pair.of(-1, d.replace("o", "test:"));
		}
		index = d.indexOf("r");
		if (index >= 0) {
			return Pair.of(3, d.replace("r", Constants.R[3]));
		}
		index = d.indexOf("p");
		if (index >= 0) {
			return Pair.of(5, d.replace("p", Constants.P[5]));
		}
		index = d.indexOf("q");
		if (index >= 0) {
			return Pair.of(5, d.replace("q", Constants.Q[5]));
		}
		index = d.indexOf("b");
		if (index >= 0) {
			return Pair.of(4, d.replace("b", "4"));
		}
		return Pair.of(-1, d);
	}

	public static void main(String[] args) {
		System.out.println(Pattern.matches(".*A\\s*,.*", "LD A  , 0x34"));
	}
}
