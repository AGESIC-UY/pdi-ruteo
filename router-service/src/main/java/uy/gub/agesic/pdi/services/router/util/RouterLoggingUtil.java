package uy.gub.agesic.pdi.services.router.util;

import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.canonical.Error;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;

public class RouterLoggingUtil {

    public static String buildMessage(Canonical<SoapPayload> message, String information) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\"dateTime\":");
        strBuilder.append("\"");
        strBuilder.append(System.currentTimeMillis());
        strBuilder.append("\",");

        strBuilder.append("\"transactionId\":");
        strBuilder.append("\"");
        strBuilder.append((String) message.getHeaders().get("transactionId"));
        strBuilder.append("\",");

        strBuilder.append("\"messageId\":");
        strBuilder.append("\"");
        strBuilder.append(message.getPayload().getWsaHeaders().get("wsaMessageID"));
        strBuilder.append("\"");

        // Si hay un error en el mensaje, lo mostramos
        if (message.getHeaders().get("error") != null) {
            Error error = (Error)message.getHeaders().get("error");

            strBuilder.append("\"errorCode\":");
            strBuilder.append("\"");
            strBuilder.append(error.getCode());
            strBuilder.append("\"");

            strBuilder.append("\"errorMessage\":");
            strBuilder.append("\"");
            strBuilder.append(error.getMessage());
            strBuilder.append("\"");

            strBuilder.append("\"errorDescription\":");
            strBuilder.append("\"");
            strBuilder.append(error.getDescription());
            strBuilder.append("\"");
        }

        // Si hay informacion extra, la mostramos
        if (information != null) {
            strBuilder.append("\"information\":");
            strBuilder.append("\"");
            strBuilder.append(information);
            strBuilder.append("\"");
        }

        return strBuilder.toString();
    }

}
