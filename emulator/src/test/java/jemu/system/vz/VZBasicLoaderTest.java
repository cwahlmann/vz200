package jemu.system.vz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import jdk.nashorn.internal.ir.annotations.Ignore;
import jemu.system.vz.export.VzBasicLoader;
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
		this.memory = new VZMemory(true);
		this.loader = new VzBasicLoader(memory);
	}

}
