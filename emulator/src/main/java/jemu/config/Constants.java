package jemu.config;

public class Constants {
    private Constants() {
    }

    public static final String LAST_WORKING_DIR = "LAST_WORKING_DIR";
    public static final String SCREEN_WIDTH = "SCREEN_WIDTH";
    public static final String SCREEN_HEIGHT = "SCREEN_HEIGHT";
    public static final String SOUND_VOLUME = "SOUND_VOLUME";
    public static final String ENABLE_DOS_ROM = "ENABLE_DOS_ROM";
    public static final String JWT_SECRET = "JWT_SECRET";
    public static final String JWT_EXPIRE_MIN = "JWT_EXPIRE_MIN";
    public static final long JWT_DEFAULT_EXPIRE_MIN = 12 * 60; // 12 hours
    public static final long JWT_HOUSKEEPING_EXPIRE_MIN = 14 * 24 * 60; // 14 days
}
