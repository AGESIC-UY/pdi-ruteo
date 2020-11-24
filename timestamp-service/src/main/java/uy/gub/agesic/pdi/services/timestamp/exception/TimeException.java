package uy.gub.agesic.pdi.services.timestamp.exception;

public class TimeException extends Exception {

    public TimeException() {
    }

    public TimeException(String message) {
        super(message);
    }

    public TimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeException(Throwable cause) {
        super(cause);
    }

    public TimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
