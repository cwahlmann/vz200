package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.rest.dto.VzSource;

import java.util.Optional;

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
