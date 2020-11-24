package uy.gub.agesic.pdi.services.router.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.services.router.soap.SoapRouterService;

@Component
public class RouterServiceImpl implements RouterService {

    private SoapRouterService soapRouterService;

    @Autowired
    public RouterServiceImpl(SoapRouterService soapRouterService) {
        this.soapRouterService = soapRouterService;
    }

    @Override
    public Canonical<SoapPayload> processSoap(Canonical<SoapPayload> message) {
        return soapRouterService.processSoap(message);
    }

    @Override
    public Canonical processJson(Canonical message) {
        throw new UnsupportedOperationException("La operaci\u00F3n seleccionada no se encuentra disponible");
    }
}
