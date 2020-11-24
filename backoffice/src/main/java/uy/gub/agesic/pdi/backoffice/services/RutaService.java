package uy.gub.agesic.pdi.backoffice.services;

import uy.gub.agesic.pdi.backoffice.dtos.FiltroRutaDTO;
import uy.gub.agesic.pdi.backoffice.dtos.RutaDTO;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import java.util.List;

public interface RutaService {

    void eliminarRutas(List<String> ids) throws BackofficeException;

    ResultadoPaginadoDTO<RutaDTO> buscarRutas(FiltroRutaDTO filtro) throws BackofficeException;

    RutaDTO obtenerRuta(String logical) throws BackofficeException;

    void crearRuta(RutaDTO ruta, String logicalOld) throws BackofficeException;

    void modificarRuta(RutaDTO ruta, String logicalOld) throws BackofficeException;

}
