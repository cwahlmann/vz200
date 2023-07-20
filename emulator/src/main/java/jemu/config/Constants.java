package jemu.config;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class Constants {
    private Constants() {
    }

    public static final String LAST_WORKING_DIR = "LAST_WORKING_DIR";
    public static final String FULLSCREEN = "FULLSCREEN";
    public static final String SCREEN_WIDTH = "SCREEN_WIDTH";
    public static final String SCREEN_HEIGHT = "SCREEN_HEIGHT";
    public static final String SOUND_VOLUME = "SOUND_VOLUME";
    public static final String WITH_RAM_EXPANSION = "TOP_MEMORY"; // 0x9000 6K on board / 0xD000 +16K Expansion
    public static final String ENABLE_DOS_ROM = "ENABLE_DOS_ROM";
    public static final String JWT_SECRET = "JWT_SECRET";
    public static final String JWT_EXPIRE_MIN = "JWT_EXPIRE_MIN";
    public static final long JWT_DEFAULT_EXPIRE_MIN = 12 * 60; // 12 hours
    public static final long JWT_HOUSKEEPING_EXPIRE_MIN = 14 * 24 * 60; // 14 days
    public static final String WITH_AUSTRALIAN_GFX_MOD = "WITH_AUSTRALIAN_GFX_MOD";
}
