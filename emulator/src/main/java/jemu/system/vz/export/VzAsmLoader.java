package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.rest.dto.VzSource;
import jemu.util.assembler.z80.Assembler;
import jemu.util.diss.DissZ80;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class VzAsmLoader extends Loader<VzAsmLoader> {

    public VzAsmLoader(Memory memory) {
        super(memory);
    }

    @Override
    public void importData(VzSource source) {
        Assembler asm = new Assembler(memory);
        asm.assemble(source.getSource());
        withStartAddress(asm.getMinCursorAddress()).withEndAddress(asm.getMaxCursorAddress()).withName(source.getName());
    }

    @Override
    public VzSource exportData() {
        StringBuilder writer = new StringBuilder();
        DissZ80 da = new DissZ80();
        int start = getStartAddress() < 0 ? 0x0000 : getStartAddress();
        int end = getEndAddress() < 0 ? start + 0x0100 : getEndAddress();
        int[] address = new int[]{start};
        while (address[0] <= end) {
            int a0 = address[0];
            String asm = da.disassemble(memory, address);
            int a1 = address[0];
            writer.append(String.format("a%04x: %-16s //", a0, asm));
            for (int a = a0; a < a1; a++) {
                writer.append(String.format(" %02x", memory.readByte(a)));
            }
            writer.append("\n");
        }
        return new VzSource().withName(this.getName()).withType(VzSource.SourceType.asm).withSource(writer.toString());
    }
}
