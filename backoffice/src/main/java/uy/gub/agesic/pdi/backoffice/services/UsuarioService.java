package uy.gub.agesic.pdi.backoffice.services;

import uy.gub.agesic.pdi.backoffice.dtos.FiltroUsuarioDTO;
import uy.gub.agesic.pdi.backoffice.dtos.UsuarioDTO;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import java.util.List;

public interface UsuarioService {

    boolean authenticate(String username, String password) throws BackofficeException;

    ResultadoPaginadoDTO<UsuarioDTO> buscarUsuarios(FiltroUsuarioDTO filtro) throws BackofficeException;

    UsuarioDTO obtenerUsuario(String login) throws BackofficeException;

    void eliminarUsuarios (List<String> logins) throws BackofficeException;

    void crearUsuario (UsuarioDTO usuario) throws BackofficeException;

    void modificarUsuario(UsuarioDTO usuario) throws BackofficeException;

    boolean cambiarContrasena(String login, String oldPassword, String newPassword) throws BackofficeException;

    String permisoUsuario(String login) throws BackofficeException;

}
