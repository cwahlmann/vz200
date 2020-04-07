package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.exception.JemuException;
import jemu.rest.dto.VzSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class VzFileLoader extends Loader<VzFileLoader> {
    private static final Logger log = LoggerFactory.getLogger(VzFileLoader.class);

    public VzFileLoader(Memory memory) {
        super(memory);
    }

    @Override
    public void importData(VzSource source) {
        ByteArrayInputStream is =
                new ByteArrayInputStream(Base64.getDecoder().decode(source.getSource()));
        try {
            byte[] header = new byte[24];
            is.read(header);
            withName(decodeName(header));
            withAutorun(decodeAutorun(header));
            withStartAddress(decodeStartAddress(header));
            int address = getStartAddress();

            int read;
            do {
                read = is.read();
                if (read != -1) {
                    memory.writeByte(address, read);
                    address = (address + 1) & 0xffff;
                }
            } while (read != -1);
            withEndAddress(address);
            if (!isAutorun()) {
                memory.writeWord(BASIC_START, getStartAddress());
                memory.writeWord(BASIC_END, getEndAddress());
            }
        } catch (IOException e) {
            log.error("Unexpected error reading basic source", e);
        }
    }

    @Override
    public VzSource exportData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStream out64 = Base64.getEncoder().wrap(out)) {
            int type = isAutorun() ? 0xf1 : 0xf0;
            int endOfBasicPointer;
            int startOfBasicPointer;
            if (getStartAddress() < 0 || getEndAddress() < 0) {
                endOfBasicPointer = memory.readWord(BASIC_END);
                startOfBasicPointer = memory.readWord(BASIC_START);
            } else {
                startOfBasicPointer = getStartAddress();
                endOfBasicPointer = getEndAddress();
            }
            byte[] header = new byte[24];
            header[21] = (byte) type;
            header[22] = (byte) (startOfBasicPointer & 0xff);
            header[23] = (byte) ((startOfBasicPointer >> 8) & 0xff);
            header[0] = 'V';
            header[1] = 'Z';
            header[2] = 'F';
            header[3] = '0';
            for (int i = 4; i < 21; i++) {
                header[i] = (i - 4 < getName().length()) ? (byte) getName().charAt(i - 4) : 0;
            }
            out64.write(header);
            for (int address = startOfBasicPointer; address < endOfBasicPointer; address++) {
                out64.write(memory.readByte(address));
            }
            out64.flush();
            return new VzSource().withName(getName()).withType(VzSource.SourceType.vz).withSource(out.toString(StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            throw new JemuException("Unexpected error writing vz-file to base64 stream", e);
        }
    }

    // utility

    public static String decodeName(byte[] header) {
        int endOfName = 4;
        while (endOfName < 21 && (header[endOfName] & 0xff) != 0) {
            endOfName++;
        }
        return new String(header, 4, endOfName - 4, StandardCharsets.US_ASCII);
    }

    public static boolean decodeAutorun(byte[] header) {
        return (header[21] & 0xff) == 0xf1;
    }

    public static int decodeStartAddress(byte[] header) {
        return (header[22] & 0xff) + 256 * (header[23] & 0xff);
    }
}
