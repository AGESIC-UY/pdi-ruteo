package uy.gub.agesic.pdi.services.router.service;

import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;

public interface RouterService {

    Canonical<SoapPayload> processSoap(Canonical<SoapPayload> message);

    Canonical processJson(Canonical message);
}
