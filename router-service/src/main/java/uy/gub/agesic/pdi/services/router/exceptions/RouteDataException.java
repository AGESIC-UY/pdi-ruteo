package uy.gub.agesic.pdi.services.router.exceptions;

public class RouteDataException extends Exception {

    public RouteDataException() {
    }

    public RouteDataException(String message) {
        super(message);
    }

    public RouteDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteDataException(Throwable cause) {
        super(cause);
    }

    public RouteDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
