package jemu.rest.dto;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class VzSource {
    private String name;
    private boolean autorun;
    private String source;
    private List<Lib> libs = new ArrayList<>();
    private SourceType type;

    public enum SourceType {
        UNDEF(".undef"), basic(".bas"), asm(".asm"), hex(".hex"), vz(".vz");
        private String extension;

        SourceType(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public static SourceType findByExtension(String extension) {
            return Stream.of(SourceType.values()).filter(v -> v.extension.equals(extension)).findAny()
                         .orElse(SourceType.UNDEF);
        }
    }

    public static class Lib {
        private String name;
        private String source;

        public String getName() {
            return name;
        }

        public Lib withName(String name) {
            this.name = name;
            return this;
        }

        public String getSource() {
            return source;
        }

        public Lib withSource(String source) {
            this.source = source;
            return this;
        }
    }

    public SourceType getType() {
        return type;
    }

    public VzSource withType(SourceType type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public VzSource withName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAutorun() {
        return autorun;
    }

    public VzSource withAutorun(boolean autorun) {
        this.autorun = autorun;
        return this;
    }

    public String getSource() {
        return source;
    }

    public VzSource withSource(String source) {
        this.source = source;
        return this;
    }

    public List<Lib> getLibs() {
        return libs;
    }

    public VzSource withLib(String name, String source) {
        libs.add(new Lib().withName(name).withSource(source));
        return this;
    }
}
