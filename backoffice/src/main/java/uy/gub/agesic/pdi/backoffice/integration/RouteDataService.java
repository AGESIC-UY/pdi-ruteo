package uy.gub.agesic.pdi.backoffice.integration;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uy.gub.agesic.pdi.backoffice.dtos.FiltroRutaDTO;
import uy.gub.agesic.pdi.backoffice.dtos.RutaDTO;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.RouteDataException;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

@FeignClient("router-service")
public interface RouteDataService {

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/routes", method = RequestMethod.POST)
    ResponseEntity<Canonical<ResultadoPaginadoDTO<RutaDTO>>> getRoutes(@RequestBody Canonical<FiltroRutaDTO> filtroMessage) throws RouteDataException;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route/{logical}", method = RequestMethod.GET)
    ResponseEntity<Canonical<RutaDTO>> getRoute(@PathVariable("logical") String logical) throws RouteDataException;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route/{logical}", method = RequestMethod.DELETE)
    void deleteRoute(@PathVariable("logical") String logical) throws RouteDataException;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route", method = RequestMethod.POST)
    void createRoute(@RequestBody Canonical<RutaDTO> ruta) throws RouteDataException;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route", method = RequestMethod.PUT)
    void updateRoute(@RequestBody Canonical<RutaDTO> ruta) throws RouteDataException;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/route/cache/{logical}", method = RequestMethod.DELETE)
    void deleteRouteFromCache(@PathVariable("logical") String logical) throws RouteDataException;

}
