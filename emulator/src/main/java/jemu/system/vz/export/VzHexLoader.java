package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.rest.dto.VzSource;

import java.io.BufferedReader;
import java.io.StringReader;

public class VzHexLoader extends Loader<VzHexLoader> {

    public VzHexLoader(Memory memory) {
        super(memory);
    }

    static class HexFileAddress {
        int min;
        int max;
        private int address;

        HexFileAddress(int address) {
            this.address = address;

            this.min = this.address;
            this.max = this.address;
        }

        public void setAddress(int address) {
            this.address = address;
            if (address < this.min) {
                this.min = address;
            }
            if (address > this.max) {
                this.max = address;
            }
        }

        public int getAddress() {
            return address;
        }
    }


    @Override
    public void importData(VzSource source) {
        BufferedReader reader = new BufferedReader(new StringReader(source.getSource()));
        HexFileAddress hfa = new HexFileAddress(getStartAddress() < 0 ? ADR : getStartAddress());
        reader.lines().forEach(line -> {
            int indexAddress = line.indexOf(':');
            String bytes = line.trim();
            if (indexAddress > 0) {
                String startString = line.substring(0, indexAddress).trim();
                hfa.setAddress(Integer.valueOf(startString, 16));
                bytes = line.substring(indexAddress + 1).trim();
            }
            int indexComment = bytes.indexOf("//");
            if (indexComment > 0) {
                bytes = bytes.substring(0, indexComment).trim();
            }
            for (String b : bytes.split(" ")) {
                memory.writeByte(hfa.getAddress(), Integer.valueOf(b.trim(), 16));
                hfa.setAddress((hfa.getAddress() + 1) & 0xffff);
            }
        });
        withStartAddress(hfa.min).withEndAddress(hfa.max).withName(source.getName());
    }

    @Override
    public VzSource exportData() {
        StringBuilder writer = new StringBuilder();
        int n = 0;
        int width = 16;
        int a = getStartAddress() < 0 ? 0x0000 : getStartAddress();
        int b = getEndAddress() < 0 ? a + 0x1000 : getEndAddress();
        StringBuilder ascii = new StringBuilder();
        for (int i = a; i < b; i++) {
            if (n % width == 0) {
                writer.append(String.format("%04x:", i));
            }
            int value = memory.readByte(i);
            ascii.append(value >= 32 && value < 127 ? (char) value : ".");
            writer.append(String.format(" %02x", value));
            n++;
            if (n % width == 0) {
                writer.append(" // ").append(ascii).append("\n");
                ascii = new StringBuilder();
            }
        }
        if ((b - a) % 16 > 0) {
            writer.append(" // ").append(ascii).append("\n");
        }
        return new VzSource().withName(getName()).withType(VzSource.SourceType.hex).withSource(writer.toString());
    }
}
