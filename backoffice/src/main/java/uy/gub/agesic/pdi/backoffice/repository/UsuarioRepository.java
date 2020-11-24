package uy.gub.agesic.pdi.backoffice.repository;

import uy.gub.agesic.pdi.backoffice.dtos.FiltroUsuarioDTO;
import uy.gub.agesic.pdi.backoffice.dtos.UsuarioDTO;
import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;


public interface UsuarioRepository {

    ResultadoPaginadoDTO<UsuarioDTO> getUsuarios(FiltroUsuarioDTO filtro) throws PDIException;
    UsuarioDTO getUsuario(String login) throws PDIException;
    void insertarUsuario(UsuarioDTO usuario) throws PDIException;
    void eliminarUsuario(String login) throws PDIException;
    void limpiarCache();

}
