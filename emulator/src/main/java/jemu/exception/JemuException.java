package jemu.exception;

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
