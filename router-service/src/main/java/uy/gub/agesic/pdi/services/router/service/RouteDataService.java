package uy.gub.agesic.pdi.services.router.service;

import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;
import uy.gub.agesic.pdi.services.router.domain.FiltroRutaDTO;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;

import java.util.List;

public interface RouteDataService {

    Boolean existeRuta(String logical) throws PDIException;

    RutaDTO obtenerRuta(String logical) throws PDIException;

    List<RutaDTO> obtenerTodasLasRutas() throws PDIException;

    ResultadoPaginadoDTO<RutaDTO> buscarRutas(FiltroRutaDTO filtro) throws PDIException;

    void eliminarRuta(String logical) throws PDIException;

    void insertarRuta(RutaDTO route, String logicalOld) throws PDIException;

    void limpiarCache(String logical) throws PDIException;

}