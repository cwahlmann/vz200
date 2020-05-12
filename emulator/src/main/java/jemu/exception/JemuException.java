package jemu.exception;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class JemuException extends RuntimeException {
    public JemuException() {
    }

    public JemuException(String message) {
        super(message);
    }

    public JemuException(String message, Throwable cause) {
        super(message, cause);
    }

    public JemuException(Throwable cause) {
        super(cause);
    }

    public JemuException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
