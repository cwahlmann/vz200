package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.rest.dto.VzSource;

import java.util.Optional;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class StaticLoaderFactory {
    private StaticLoaderFactory() {
    }

    public static Optional<Loader<?>> create(VzSource.SourceType type, Memory memory) {
        switch (type) {
            case asm:
                return Optional.of(new VzAsmLoader(memory));
            case basic:
                return Optional.of(new VzBasicLoader(memory));
            case hex:
                return Optional.of(new VzHexLoader(memory));
            case vz:
                return Optional.of(new VzFileLoader(memory));
            default:
                return Optional.empty();
        }
    }
}
