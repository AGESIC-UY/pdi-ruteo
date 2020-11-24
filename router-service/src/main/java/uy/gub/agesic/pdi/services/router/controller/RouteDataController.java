package uy.gub.agesic.pdi.services.router.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.utiles.ErrorUtil;
import uy.gub.agesic.pdi.common.soap.DataUtil;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;
import uy.gub.agesic.pdi.services.router.domain.FiltroRutaDTO;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;
import uy.gub.agesic.pdi.services.router.exceptions.RouteDataException;
import uy.gub.agesic.pdi.services.router.service.RouteDataService;


@RestController
public class RouteDataController {

    private static final Logger logger = LoggerFactory.getLogger(RouteDataController.class);

    private static final String LOGICALOLD_HEADER_PARAM_NAME = "logicalOld";
    private RouteDataService routeDataService;

    @Autowired
    public RouteDataController(RouteDataService routeDataService) {
        this.routeDataService = routeDataService;
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/routes", method = RequestMethod.POST)
    public ResponseEntity<Canonical<ResultadoPaginadoDTO<RutaDTO>>> getRoutes(@RequestBody Canonical<FiltroRutaDTO> filtroMessage) throws RouteDataException {
        Canonical<ResultadoPaginadoDTO<RutaDTO>> c = new Canonical<ResultadoPaginadoDTO<RutaDTO>>();

        try {
            ResultadoPaginadoDTO<RutaDTO> resultado = routeDataService.buscarRutas(filtroMessage.getPayload());
            c.setPayload(resultado);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            c.getHeaders().put("error", ErrorUtil.createError(e));
        }

        return new ResponseEntity<Canonical<ResultadoPaginadoDTO<RutaDTO>>>(c, HttpStatus.OK);
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route/{logical}", method = RequestMethod.GET)
    public ResponseEntity<Canonical<RutaDTO>> getRoute(@PathVariable("logical") String logical) throws RouteDataException {
        Canonical<RutaDTO> c = new Canonical<RutaDTO>();

        try {
            String decoded = DataUtil.decodeToString(logical);
            RutaDTO route = routeDataService.obtenerRuta(decoded);
            if (route != null) {
                c.setPayload(route);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            c.getHeaders().put("error", ErrorUtil.createError(e));
        }

        return new ResponseEntity<Canonical<RutaDTO>>(c, HttpStatus.OK);
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route/{logical}", method = RequestMethod.DELETE)
    public void deleteRoute(@PathVariable("logical") String logical) throws RouteDataException {
        try {
            String decoded = new String(DataUtil.decode(logical));
            routeDataService.eliminarRuta(decoded);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RouteDataException(e);
        }
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route", method = RequestMethod.POST)
    public void createRoute(@RequestBody Canonical<RutaDTO> routeMessage) throws RouteDataException {
        RutaDTO route = routeMessage.getPayload();

        try {
            if (this.routeDataService.existeRuta(route.getLogical())) {
                throw new RouteDataException("La ruta con id l\u00F3gico: " + route.getLogical() + " ya existe");
            }

            routeDataService.insertarRuta(route, null);
        } catch (RouteDataException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RouteDataException(e);
        }
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route", method = RequestMethod.PUT)
    public void updateRoute(@RequestBody Canonical<RutaDTO> routeMessage) throws RouteDataException {
        RutaDTO route = routeMessage.getPayload();
        String logicalOld = null;

        try {
            if(routeMessage.getHeaders().get(LOGICALOLD_HEADER_PARAM_NAME) != null)
                logicalOld = routeMessage.getHeaders().get(LOGICALOLD_HEADER_PARAM_NAME).toString();

            if (!this.routeDataService.existeRuta(logicalOld)) {
                throw new RouteDataException("La ruta con id l\u00F3gico: " + logicalOld + " no existe");
            }

            routeDataService.insertarRuta(route, routeMessage.getHeaders().get("logicalOld").toString());
        } catch (RouteDataException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RouteDataException(e);
        }

    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route/cache/{logical}", method = RequestMethod.DELETE)
    public void deleteRouteFromCache(@PathVariable("logical") String logical) throws RouteDataException {
        try {
            String decoded = new String(DataUtil.decode(logical));
            routeDataService.limpiarCache(decoded);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RouteDataException(e);
        }
    }

}
