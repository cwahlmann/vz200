package de.dreierschach.vz200ui.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lib {
        private String name;
        private String source;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutorun() {
        return autorun;
    }

    public void setAutorun(boolean autorun) {
        this.autorun = autorun;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Lib> getLibs() {
        return libs;
    }

    public void setLibs(List<Lib> libs) {
        this.libs = libs;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }
}
