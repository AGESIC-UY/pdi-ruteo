package uy.gub.agesic.pdi.services.router.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.services.router.service.RouterService;

@RestController()
public class RouterController {

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);

    private RouterService routerBusiness;

    @Autowired
    public RouterController(RouterService routerBusiness) {
        this.routerBusiness = routerBusiness;
    }

    @RequestMapping(value = "/router", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Canonical processJson(@RequestBody Canonical canonical) {
        String type = (String) canonical.getHeaders().get("type");
        if (type.equals("soap")) {
            return this.routerBusiness.processSoap(canonical);
        } else {
            return this.routerBusiness.processJson(canonical);
        }

    }

}
