package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.rest.VzSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;

public class VzBasicLoader extends Loader<VzBasicLoader> {
    private static final Logger log = LoggerFactory.getLogger(VzBasicLoader.class);

    /* set to 1 for Colour Genie tokenizer, 0 for VZ200/300 */

    static class SourceLine {
        public SourceLine(String text) {
            this.index = 0;
            this.text = text;
            this.comment = false;
        }

        String text;
        boolean comment;
        int index;
    }

    static class Destination {
        public Destination(Memory memory, int address) {
            this.memory = memory;
            this.address = address;
            this.addressCurrentLine = address;
        }

        Memory memory;
        int addressCurrentLine;
        int address;
    }

    static final String[] TOKENS = new String[]{ //
                                                 "END", "FOR", "RESET", "SET", "CLS", ""/* CMD */, "RANDOM", "NEXT",
                                                 // 80-87
                                                 "DATA", "INPUT", "DIM", "READ", "LET", "GOTO", "RUN", "IF", // 88-8f
                                                 "RESTORE", "GOSUB", "RETURN", "REM", "STOP", "ELSE", "COPY", "COLOR",
                                                 // 90-97
                                                 "VERIFY", "DEFINT", "DEFSNG", "DEFDBL", "CRUN", "MODE", "SOUND",
                                                 "RESUME", // 98-9f
                                                 "OUT", "ON", "OPEN", "FIELD", "GET", "PUT", "CLOSE", "LOAD", // a0-a7
                                                 "MERGE", "NAME", "KILL", "LSET", "RSET", "SAVE", "SYSTEM", "LPRINT",
                                                 // a8-af
                                                 "DEF", "POKE", "PRINT", "CONT", "LIST", "LLIST", "DELETE", "AUTO",
                                                 // b0-b7
                                                 "CLEAR", "CLOAD", "CSAVE", "NEW", "TAB(", "TO", "FN", "USING", // b8-bf
                                                 "VARPTR", "USR", "ERL", "ERR", "STRING$", "INSTR", "POINT", "TIME$",
                                                 // c0-c7
                                                 "MEM", "INKEY$", "THEN", "NOT", "STEP", "+", "-", "*", // c8-cf
                                                 "/", "^", "AND", "OR", ">", "=", "<", "SGN", // d0-d7
                                                 "INT", "ABS", "FRE", "INP", "POS", "SQR", "RND", "LOG", // d8-df
                                                 "EXP", "COS", "SIN", "TAN", "ATN", "PEEK", "CVI", "CVS", // e0-e7
                                                 "CVD", "EOF", "LOC", "LOF", "MKI$", "MKS$", "MKD$", "CINT", // e8-ef
                                                 "CSNG", "CDBL", "FIX", "LEN", "STR$", "VAL", "ASC", "CHR$", // f0-f7
                                                 "LEFT$", "RIGHT$", "MID$", "'", "", "", "", ""}; // f8-ff

    static final int TOKEN_OFFSET = 0x80;

    public VzBasicLoader(Memory memory) {
        super(memory);
    }

    @Override
    public void importData(VzSource source) {
        Destination destination = new Destination(memory, ADR);
        BufferedReader reader = new BufferedReader(new StringReader(source.getSource()));
        reader.lines().forEach(text -> {
            SourceLine sourceLine = new SourceLine(text.trim().toUpperCase().replaceAll("\t", "    "));
            if (!sourceLine.text.isEmpty()) {
                destination.address += 2;
                skipWhiteSpace(sourceLine);
                readLineNumber(sourceLine, destination);
                skipWhiteSpace(sourceLine);
                while (sourceLine.index < sourceLine.text.length()) {
                    if (sourceLine.comment) {
                        readChar(sourceLine, destination);
                    } else {
                        boolean checked = checkToken(sourceLine, destination) || checkString(sourceLine, destination);
                        if (!checked) {
                            readChar(sourceLine, destination);
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
        withStartAddress(ADR).withEndAddress(destination.address).withName(source.getName());
    }

    @Override
    public VzSource exportData() {
        StringBuilder writer = new StringBuilder();
        int address = memory.readWord(BASIC_START);
        int endAddress = memory.readWord(BASIC_END);
        boolean finished = false;
        while (!finished && address < endAddress) {
            int nextLineAddress = memory.readWord(address);
            if (nextLineAddress == 0) {
                finished = true;
            } else {
                address += 2;
                int lineNo = memory.readWord(address);
                address += 2;
                writer.append(String.format("%d ", lineNo));
                while (address < nextLineAddress - 1) {
                    int b = memory.readByte(address);
                    address++;
                    if (b < 0) {
                        b = b + 256;
                    }
                    if (b >= 0 && b < TOKEN_OFFSET) {
                        writer.append((char) b);
                    } else {
                        writer.append(TOKENS[b - TOKEN_OFFSET]);
                    }
                }
                address++;
                writer.append('\n');
            }
        }
        return new VzSource().withName(this.getName()).withType(VzSource.SourceType.basic)
                             .withSource(writer.toString());
    }

    // ------- private methods

    private boolean checkToken(SourceLine sourceLine, Destination destination) {

        for (int t = 0; t < TOKENS.length; t++) {
            if (!TOKENS[t].isEmpty() && sourceLine.index + TOKENS[t].length() <= sourceLine.text.length() &&
                sourceLine.text.substring(sourceLine.index, sourceLine.index + TOKENS[t].length())
                               .equalsIgnoreCase(TOKENS[t])) {
                destination.memory.writeByte(destination.address, t + TOKEN_OFFSET);
                destination.address++;
                sourceLine.index += TOKENS[t].length();
                if (TOKENS[t].equals("REM")) {
                    sourceLine.comment = true;
                }
                return true;
            }
        }
        return false;
    }

    private void readLineNumber(SourceLine sourceLine, Destination destination) {
        int n = 0;
        while (sourceLine.index < sourceLine.text.length() &&
               Character.isDigit(sourceLine.text.charAt(sourceLine.index))) {
            n = n * 10 + (sourceLine.text.charAt(sourceLine.index) - '0');
            sourceLine.index++;
        }
        destination.memory.writeWord(destination.address, n);
        destination.address += 2;
    }

    private boolean checkString(SourceLine sourceLine, Destination destination) {
        if (sourceLine.text.charAt(sourceLine.index) != '"') {
            return false;
        }
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
        return true;
    }

    private void skipWhiteSpace(SourceLine sourceLine) {
        while (sourceLine.index < sourceLine.text.length() && isWhitespace(sourceLine.text.charAt(sourceLine.index))) {
            sourceLine.index++;
        }
    }

    private void readChar(SourceLine sourceLine, Destination destination) {
        destination.memory.writeByte(destination.address, sourceLine.text.charAt(sourceLine.index));
        destination.address++;
        sourceLine.index++;
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }
}
