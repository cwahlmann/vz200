package jemu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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
