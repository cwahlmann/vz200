package jemu.rest;

import java.util.stream.Stream;

public class VzSource {
    private String name;
    private boolean autorun;
    private String source;
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
}
