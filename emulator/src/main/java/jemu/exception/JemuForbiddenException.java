package jemu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@ResponseStatus(HttpStatus.FORBIDDEN)
public class JemuForbiddenException extends RuntimeException {
    public JemuForbiddenException() {
    }

    public JemuForbiddenException(String message) {
        super(message);
    }

    public JemuForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public JemuForbiddenException(Throwable cause) {
        super(cause);
    }

    public JemuForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
