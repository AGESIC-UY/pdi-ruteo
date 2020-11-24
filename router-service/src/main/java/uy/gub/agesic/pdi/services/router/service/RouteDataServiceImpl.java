package uy.gub.agesic.pdi.services.router.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;
import uy.gub.agesic.pdi.services.router.domain.FiltroRutaDTO;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;
import uy.gub.agesic.pdi.services.router.repository.RouteDataRepository;

import java.util.List;

@Service
public class RouteDataServiceImpl implements RouteDataService {

    private static final Logger logger = LoggerFactory.getLogger(RouteDataServiceImpl.class);

    private RouteDataRepository routesRepository;

    @Autowired
    public RouteDataServiceImpl(RouteDataRepository routesRepository) {
        this.routesRepository = routesRepository;
    }

    @Override
    public Boolean existeRuta(String logical) throws PDIException {
        return routesRepository.existeRuta(logical);
    }

    @Override
    public RutaDTO obtenerRuta(String logical) throws PDIException {
        return routesRepository.obtenerRuta(logical);
    }

    @Override
    public List<RutaDTO> obtenerTodasLasRutas() throws PDIException {
        List<RutaDTO> rutas = routesRepository.obtenerTodasLasRutas();
        return rutas;
    }

    @Override
    public ResultadoPaginadoDTO<RutaDTO> buscarRutas(FiltroRutaDTO filtro) throws PDIException {
        return routesRepository.buscarRutas(filtro);
    }

    @Override
    public void eliminarRuta(String logical) throws PDIException {
        routesRepository.eliminarRuta(logical);
    }

    @Override
    public void insertarRuta(RutaDTO route, String logicalOld) throws PDIException {
        routesRepository.agregarRuta(route, logicalOld);
    }

    @Override
    public void limpiarCache(String logical) throws PDIException {
        routesRepository.limpiarCache();
    }

}


/*
    public void setRouteDegradationStatus(String logical, Boolean degradeStatus) {
        RutaDTO route = routesRepository.obtenerRuta(logical);
        route.setDegraded(degradeStatus);
        routesRepository.agregarRuta(route);
    }
 */