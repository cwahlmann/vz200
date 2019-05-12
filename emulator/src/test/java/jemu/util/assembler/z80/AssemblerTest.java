package jemu.util.assembler.z80;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssemblerTest {

	private Assembler assembler;
	private TestMemory memory;

	@BeforeEach
	public void init() {
		this.memory = new TestMemory();
		this.assembler = new Assembler(this.memory);
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
		//@formatter:off
		String[] source = { 
				"start: LD A, (var1:)", 
				"       RET", "var1:  defb 0x34" 
				};
		//@formatter:on
		int[] expected = { 0x3a, 0x04, 0x00, 0xc9, 0x34 };
		assertParse(0, expected, source);
	}

	@Test
	public void parseStatementTest() {
		//@formatter:off
		String[] source = { 
				".def var1: 0x3f49", 
				".org 0x0100", 
				".run 0x0101", 
				"var2: defb 0x12", 
				"LD A, (var1:)",
				"LD A, (var2:)", 
				};
		//@formatter:on
		int[] expected = { 0x12, 0x3a, 0x49, 0x3f, 0x3a, 0x00, 0x01 };
		assertParse(0x0100, expected, source);
		assertEquals(0x0101, assembler.getRunAddress());
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
		//@formatter:off
		int[] expected = {
				0xc3, 0x0e, 0x80,
				0x08, 0x01, 0x0c, 0x0c, 0x0f, 0x20, 0x17, 0x05, 0x0c, 0x14, 0x00, 
				0x21, 0x03, 0x80, 
				0x01, 0x00, 0x70, 
				0x7e, 
				0xb7, 
				0xc8,
				0x02,
				0x23, 
				0x03, 
				0x18, 0xf8, 
			};
		//@formatter:on
		assembler.assemble(path);
		assertMem(0x8000, expected);
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
