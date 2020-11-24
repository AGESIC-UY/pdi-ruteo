package uy.gub.agesic.pdi.services.router.repository;

import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;
import uy.gub.agesic.pdi.services.router.domain.FiltroRutaDTO;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;

import java.util.List;

public interface RouteDataRepository {

    ResultadoPaginadoDTO<RutaDTO> buscarRutas(FiltroRutaDTO filtro) throws PDIException;

    RutaDTO obtenerRuta(String logical) throws PDIException;

    List<RutaDTO> obtenerTodasLasRutas() throws PDIException;

    Boolean existeRuta(String logical) throws PDIException;

    void agregarRuta(RutaDTO route, String logicalOld) throws PDIException;

    void eliminarRuta(String logical) throws PDIException;

    void limpiarCache();
}