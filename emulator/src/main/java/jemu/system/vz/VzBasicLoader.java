package jemu.system.vz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.core.device.memory.Memory;

public class VzBasicLoader {
	private static final Logger log = LoggerFactory.getLogger(VzBasicLoader.class);

	/* set to 1 for Colour Genie tokenizer, 0 for VZ200/300 */

	class SourceLine {
		public SourceLine(String text) {
			this.index = 0;
			this.text = text;
			this.comment = false;
		}

		String text;
		boolean comment;
		int index;
	}

	class Destination {
		public Destination(Memory memory, int address) {
			this.memory = memory;
			this.address = address;
			this.addressCurrentLine = address;
		}

		Memory memory;
		int addressCurrentLine;
		int address;
	}

	Destination destination;

	static final String EXT = ".vz";
	static final int ADR = 0x7ae9;
	static final int BASIC_START = 0x78a4;
	static final int BASIC_END = 0x78f9;

	static final String[] token = new String[] { "END", "FOR", "RESET", "SET", "CLS", ""/* CMD */, "RANDOM", "NEXT",
			"DATA", "INPUT", "DIM", "READ", "LET", "GOTO", "RUN", "IF", "RESTORE", "GOSUB", "RETURN", "REM", "STOP",
			"ELSE", "COPY", "COLOR", "VERIFY", "DEFINT", "DEFSNG", "DEFDBL", "CRUN", "MODE", "SOUND", "RESUME", "OUT",
			"ON", "OPEN", "FIELD", "GET", "PUT", "CLOSE", "LOAD", "MERGE", "NAME", "KILL", "LSET", "RSET", "SAVE",
			"SYSTEM", "LPRINT", "DEF", "POKE", "PRINT", "CONT", "LIST", "LLIST", "DELETE", "AUTO", "CLEAR", "CLOAD",
			"CSAVE", "NEW", "TAB(", "TO", "FN", "USING", "VARPTR", "USR", "ERL", "ERR", "STRING$", "INSTR", "POINT",
			"TIME$", "MEM", "INKEY$", "THEN", "NOT", "STEP", "+", "-", "*", "/", "^", "AND", "OR", ">", "=", "<", "SGN",
			"INT", "ABS", "FRE", "INP", "POS", "SQR", "RND", "LOG", "EXP", "COS", "SIN", "TAN", "ATN", "PEEK", "CVI",
			"CVS", "CVD", "EOF", "LOC", "LOF", "MKI$", "MKS$", "MKD$", "CINT", "CSNG", "CDBL", "FIX", "LEN", "STR$",
			"VAL", "ASC", "CHR$", "LEFT$", "RIGHT$", "MID$", "'", "", "", "", "" };

	public VzBasicLoader(Memory memory) {
		this.destination = new Destination(memory, ADR);
	}

	private boolean checkToken(SourceLine sourceLine) {
		int tokenOffset = 0x80;
		for (int t = 0; t < token.length; t++) {
			if (!token[t].isEmpty() && sourceLine.index + token[t].length() <= sourceLine.text.length()
					&& sourceLine.text.substring(sourceLine.index, sourceLine.index + token[t].length())
							.equalsIgnoreCase(token[t])) {
				destination.memory.writeByte(destination.address, t + tokenOffset);
				destination.address++;
				sourceLine.index += token[t].length();
				System.out.println("found token " + token[t]);
				if (token[t].equals("REM")) {
					sourceLine.comment = true;
				}
				return true;
			}
		}
		return false;
	}

	private void readLineNumber(SourceLine sourceLine) {
		int n = 0;
		while (sourceLine.index < sourceLine.text.length()
				&& Character.isDigit(sourceLine.text.charAt(sourceLine.index))) {
			n = n * 10 + (sourceLine.text.charAt(sourceLine.index) - '0');
			sourceLine.index++;
		}
		destination.memory.writeWord(destination.address, n);
		destination.address += 2;
	}

	private boolean checkString(SourceLine sourceLine) {
		if (sourceLine.text.charAt(sourceLine.index) != '"') {
			return false;
		}
		System.out.println("found string");
		destination.memory.writeByte(destination.address, '"');
		destination.address++;
		sourceLine.index++;
		while (sourceLine.index < sourceLine.text.length() && sourceLine.text.charAt(sourceLine.index) != '"') {
			char c = sourceLine.text.charAt(sourceLine.index);
			if (c == '\t') {
				for (int i = 0; i < 4; i++) {
					destination.memory.writeByte(destination.address, ' ');
					destination.address++;
				}
			} else {
				destination.memory.writeByte(destination.address, sourceLine.text.charAt(sourceLine.index));
				destination.address++;
			}
			sourceLine.index++;
		}
		destination.memory.writeByte(destination.address, '"');
		destination.address++;
		sourceLine.index++;
		System.out.println("found done");
		return true;
	}

	private void skipWhiteSpace(SourceLine sourceLine) {
		while (sourceLine.index < sourceLine.text.length() && isWhitespace(sourceLine.text.charAt(sourceLine.index))) {
			sourceLine.index++;
		}
	}

	private void readChar(SourceLine sourceLine) {
		destination.memory.writeByte(destination.address, sourceLine.text.charAt(sourceLine.index));
		destination.address++;
		sourceLine.index++;
	}

	private boolean isWhitespace(char c) {
		return c == ' ' || c == '\t';
	}

	void loadBasFile(InputStream is) throws IOException {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			reader.lines().forEach(text -> {
				System.out.println(text);
				SourceLine sourceLine = new SourceLine(text.trim().toUpperCase());
				if (!sourceLine.text.isEmpty()) {
					destination.address += 2;
					skipWhiteSpace(sourceLine);
					readLineNumber(sourceLine);
					while (sourceLine.index < sourceLine.text.length()) {
						skipWhiteSpace(sourceLine);

						if (sourceLine.comment) {
							readChar(sourceLine);
						} else {
							boolean checked = checkToken(sourceLine) || checkString(sourceLine);
							if (!checked) {
								readChar(sourceLine);
							}
						}
					}
					destination.memory.writeByte(destination.address, 0x00);
					destination.address++;
					destination.memory.writeWord(destination.addressCurrentLine, destination.address);
					destination.addressCurrentLine = destination.address;
				}
			});
			for (int i = 0; i < 2; i++) {
				destination.memory.writeByte(destination.address, 0x00);
				destination.address++;
			}
			destination.memory.writeWord(BASIC_START, ADR);
			destination.memory.writeWord(BASIC_END, destination.address);
			log.info("Laden des Inputstreams erledigt.");
		} catch (Exception e) {
			log.error("Fehler beim Lesen des Inputstreams", e);
		}
	}
}
