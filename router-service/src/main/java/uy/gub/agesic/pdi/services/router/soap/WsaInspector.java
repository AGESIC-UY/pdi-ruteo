package uy.gub.agesic.pdi.services.router.soap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.soap.WsaHeadersProcessor;
import uy.gub.agesic.pdi.services.router.exceptions.SoapRouterException;
import uy.gub.agesic.pdi.services.router.util.Constants;

@Component
public class WsaInspector {

    private static final Logger logger = LoggerFactory.getLogger(WsaInspector.class);

    @Autowired
    public WsaInspector() {
    }

    @Loggable
    void processHeaders(Canonical<SoapPayload> message, String xml) throws SoapRouterException {
        try {
            WsaHeadersProcessor.processHeaders(message, xml);
        } catch (Exception e) {
            throw new SoapRouterException("Error al procesar el cabezal, inv\u00E1lido", null, Constants.ERRORPROCHEADER, e);
        }
    }

    String getWsaAction(Canonical<SoapPayload> message) {
        return WsaHeadersProcessor.getWsaAction(message);
    }

    String getWsaTo(Canonical<SoapPayload> message) {
        return WsaHeadersProcessor.getWsaTo(message);
    }

    String getWsaMessageID(Canonical<SoapPayload> message) {
        return WsaHeadersProcessor.getWsaMessageID(message);
    }

}
