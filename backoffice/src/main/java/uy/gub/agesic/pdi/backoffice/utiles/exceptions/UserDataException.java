package uy.gub.agesic.pdi.backoffice.utiles.exceptions;

public class UserDataException extends Exception{

    public UserDataException() {
    }

    public UserDataException(String message) {
        super(message);
    }

    public UserDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDataException(Throwable cause) {
        super(cause);
    }

    public UserDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
