package jemu.rest.dto;

public class JemuVersion {
    private String version;

    public JemuVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}