package uy.gub.agesic.pdi.services.router.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SoapRouterException extends Exception {

    private String message;

    private String description;

    private String code;

    public SoapRouterException(String message, String description) {
        code = "Error Interno";
        this.message = message;
        this.description = description;
    }

    public SoapRouterException(String message, String description, String code,  Throwable e) {
        super(e);
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public SoapRouterException(Exception e) {
        this((Throwable) e);
    }

    public SoapRouterException(Throwable t) {
        if (t instanceof SoapRouterException) {
            SoapRouterException srException = (SoapRouterException) t;
            this.code = srException.getCode();
            this.description = srException.getDescription();
            this.message = srException.getMessage();
        } else {
            code = "Error Interno";
            message = t.getMessage();

            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            description = errors.toString();
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

}
