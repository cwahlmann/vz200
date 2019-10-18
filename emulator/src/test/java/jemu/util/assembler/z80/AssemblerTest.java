package jemu.util.assembler.z80;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jemu.util.assembler.z80.Constants.NumToken;
import jemu.util.assembler.z80.Constants.RegisterToken;

public class AssemblerTest {

	private Assembler assembler;
	private TestMemory memory;

	@BeforeEach
	public void init() {
		this.memory = new TestMemory();
		this.assembler = new Assembler(this.memory);
	}

	@Test
	public void testComment() {
		testComment("", "; ein Kommentar");
		testComment("", "// ein Kommentar");
		testComment("LD A, B", "LD A, B; ein Kommentar");
		testComment("defs \"ein Text\"", "defs \"ein Text\"// ein Kommentar");
		testComment("defs \"ein Text\"", "defs \"ein Text\"; ein Kommentar");
		testComment("defs \"kein // Kommentar\"", "defs \"kein // Kommentar\"");

	}

	private void testComment(String expected, String line) {
		assertEquals(expected, assembler.removeComment(line));
	}

	@Test
	public void parseLineTest() {
		String line = "LD A, 0xf3";
		assembler.setCursorAddress(0);
		assembler.getCurrentLineNo().add(0);
		assembler.parseLine(line);

		assertEquals(2, assembler.getCursorAddress());
		assertEquals(0x3e, memory.readByte(0));
		assertEquals(0xf3, memory.readByte(1));
	}

	@Test
	public void parseWithLabelTest() {
		// @formatter:off
		String[] source = { "start: LD A, (var1:)", "       RET", "var1:  defb 0x34" };
		// @formatter:on
		int[] expected = { 0x3a, 0x04, 0x00, 0xc9, 0x34};
		assertParse(0, expected, source);
	}

	@Test
	public void numTokenTest() {
		String pw = NumToken.regexWord();
		String pb= NumToken.regexWord();
		
		assertFalse(RegisterToken.parse("0q33333332").isPresent());
		assertTrue(Pattern.matches(pw, "0q33333332"));
		assertTrue(Pattern.matches(pb, "0q3332"));
	}
	
	@Test
	public void parseIntegerTest() {
		String value = "0q33333332";

		String regex = Constants.NumToken.quaWord.regex();
		assertTrue(Pattern.matches(regex, value));

		regex = Constants.NumToken.regexWord();
		assertTrue(Pattern.matches(regex, value));
		
		Command com = Command.com("LD A,(nn)").c(0x3a).nnl1().nnh1();
		assertTrue(LineParser.matches(com, "LD A, (0q.0123.01)"));
	}		
	
	@Test
	public void parseIntegerTest2() {
		// @formatter:off
		String[] source = { 
				"LD A, (0xfffe)", 
				"LD A, (65534)", 
				"LD A, (0o177776)", 
				"LD A, (0b1111111111111110)",
				"LD A, (0q33333332)",
				"LD A, ('3A')",  
				};
		int[] expected = { 
				0x3a, 0xfe, 0xff, 
				0x3a, 0xfe, 0xff, 
				0x3a, 0xfe, 0xff, 
				0x3a, 0xfe, 0xff, 
				0x3a, 0xfe, 0xff,
				0x3a, 0x33, 0x41,
				};
		// @formatter:on
		assertParse(0, expected, source);
	}

	@Test
	public void parseStatementTest() {
		// @formatter:off
		String[] source = { ".def var1: 0x3f49", ".org 0x0100", ".run 0x0101", "var2: defb 0x12", "LD A, (var1:)",
				"LD A, (var2:)", };
		// @formatter:on
		int[] expected = { 0x12, 0x3a, 0x49, 0x3f, 0x3a, 0x00, 0x01 };
		assertParse(0x0100, expected, source);
		assertEquals(0x0101, assembler.getRunAddress());
	}

	@Test
	public void parseStatementTest2() {
		// @formatter:off
		String[] source = { ".org 0x1234", ".run 0x1235", "count: defb 0x01", "LD A, (count:)", "DEC A",
				"LD (count:), A", "defw count:" };
		// @formatter:on
		int[] expected = { 0x01, 0x3a, 0x34, 0x12, 0x3d, 0x32, 0x34, 0x12, 0x34, 0x12 };
		assertParse(0x1234, expected, source);
		assertEquals(0x1235, assembler.getRunAddress());
	}

	@Test
	public void parseFileTest1() throws Exception {
		Path path = Paths.get(AssemblerTest.class.getResource("message.asm").toURI());
		int[] expected = { 0x08, 0x01, 0x0c, 0x0c, 0x0f, 0x20, 0x17, 0x05, 0x0c, 0x14, 0x00 };
		assembler.assemble(path);
		assertMem(0, expected);
	}

	@Test
	public void parseFileTest2() throws Exception {
		Path path = Paths.get(AssemblerTest.class.getResource("testsource.asm").toURI());
		// @formatter:off
		int[] expected = { 0xc3, 0x0e, 0x80, 0x08, 0x01, 0x0c, 0x0c, 0x0f, 0x20, 0x17, 0x05, 0x0c, 0x14, 0x00, 0x21,
				0x03, 0x80, 0x01, 0x00, 0x70, 0x7e, 0xb7, 0xc8, 0x02, 0x23, 0x03, 0x18, 0xf8, };
		// @formatter:on
		assembler.assemble(path);
		assertMem(0x8000, expected);
	}

	@Test
	public void parseRegisterTest() {
		// @formatter:off
		String[] source = { "LD A, A", "LD A, B", "LD A, C", "LD A, D", "LD A, E", "LD A, H", "LD A, L", "LD A, (HL)",
				"LD A, IXh", "LD A, IXl", "LD A, IYh", "LD A, IYl", };
		// @formatter:on
		int[] expected = { 0x7f, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0xdd, 0x7c, 0xdd, 0x7d, 0xfd, 0x7c, 0xfd,
				0x7d };
		assertParse(0x0000, expected, source);
		assertEquals(0x0000, assembler.getRunAddress());
	}

	private void assertParse(int org, int[] expected, String[] source) {
		assembler.setCursorAddress(0);
		assembler.getCurrentLineNo().add(0);
		for (String line : source) {
			assembler.parseLine(line);
		}
		assembler.resolveOpenTokens();
		assertMem(org, expected);
	}

	private void assertMem(int org, int[] expected) {
		assertEquals(org + expected.length, assembler.getCursorAddress());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], memory.readByte(org + i));
		}
	}
}
