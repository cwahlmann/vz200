package jemu.system.vz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import jemu.core.device.memory.Memory;

public class VZBasicLoaderTest {

	private static final int[] BYTE_CODE = { 0x00, 0x7B, // 7ae9: next address = 7b00
			0x0A, 0x00, // 7aeb: line no "10"
			0xB2, 0x20, 0x20, 0x20, 0x22, 0x48, 0x41, 0x4C, //
			0x4C, 0x4F, 0x20, 0x20, 0x57, 0x45, 0x4C, 0x54, //
			0x21, 0x22, // 7aed: PRINT "HALLO WELT!"
			0x00, // 7aff: end of line
			0x0B, 0x7B, // 7b00: next address = 7b0b
			0x14, 0x00, // 7b02: line no "20"
			0x8D, 0x20, 0x20, 0x20, 0x31, 0x30, // 7b04: GOTO 10
			0x00, // 7b0a: end of line
			0x00, 0x00 // 7b0b: end of program
	};

	private static final String SOURCE_CODE = "10   PRINT   \"HALLO  WELT!\"\n20  GOTO   10";
	private static final String EXPECTED_CODE = "10 PRINT   \"HALLO  WELT!\"\n20 GOTO   10\n";

	private Memory memory;
	private VzBasicLoader loader;

	public VZBasicLoaderTest() {
		this.memory = new VZMemory();
		this.loader = new VzBasicLoader(memory);
	}

	@Test
	public void testImportBasic() throws IOException {
		try (InputStream is = new ByteArrayInputStream(SOURCE_CODE.getBytes(Charset.defaultCharset()))) {
			loader.importBasFile(is);
		}
		int start = memory.readWord(VzBasicLoader.BASIC_START);
		assertEquals(start, VzBasicLoader.ADR);
		int end = memory.readWord(VzBasicLoader.BASIC_END);
		// StringBuilder result = new StringBuilder();
		for (int a = start; a < end; a++) {
			assertEquals(BYTE_CODE[a - start], memory.readByte(a));
		}
	}

	@Test
	public void testExportBasic() throws IOException {
		int address = VzBasicLoader.ADR;
		memory.writeWord(VzBasicLoader.BASIC_START, address);
		for (int b : BYTE_CODE) {
			memory.writeByte(address, b);
			address++;
		}
		memory.writeWord(VzBasicLoader.BASIC_END, address);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(256)) {
			loader.exportBasFile(out);
			String exported = new String(out.toByteArray(), Charset.defaultCharset());
			assertEquals(EXPECTED_CODE, exported);
		}
	}

}
