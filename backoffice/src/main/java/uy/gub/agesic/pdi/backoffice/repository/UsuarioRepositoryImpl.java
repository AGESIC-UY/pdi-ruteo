package uy.gub.agesic.pdi.backoffice.repository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.backoffice.dtos.FiltroUsuarioDTO;
import uy.gub.agesic.pdi.backoffice.dtos.UsuarioDTO;
import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.git.GitManager;
import uy.gub.agesic.pdi.common.utiles.ListaUtil;
import uy.gub.agesic.pdi.common.utiles.StringAcceptor;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ManagedResource
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private static Logger logger = LoggerFactory.getLogger(UsuarioRepositoryImpl.class);

    @Value("${application.git.fileUsuarios}")
    private String fileUsuarios;

    private GitManager gitManager;

    private Map<String, UsuarioDTO> mapUsuarios = new HashMap<>();

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public UsuarioRepositoryImpl(GitManager gitManager) {
        this.gitManager = gitManager;
    }

    @PostConstruct
    private void init() {
        boolean mustShutdown = false;

        try {
            gitManager.recibirCambios();

            // Se carga la lista de usuarios desde el archivo
            List<UsuarioDTO> usuarios = mapper.readValue(new File(gitManager.getAbsoluteLocalPath(fileUsuarios)), new TypeReference<List<UsuarioDTO>>() { });

            this.mapUsuarios.clear();
            for (UsuarioDTO usuario : usuarios) {
                this.mapUsuarios.put(usuario.getLogin(), usuario);
            }
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de usuarios", e);
            mustShutdown = true;
        } catch (JsonParseException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de usuarios", e);
            mustShutdown = true;
        } catch (JsonMappingException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de usuarios", e);
            mustShutdown = true;
        } catch (IOException e) {
            logger.error("Ha ocurrido un error inicializando el repositorio de usuarios", e);
            mustShutdown = true;
        }

        if (mustShutdown) {
            System.exit(10);
        }
    }

    @Override
    public ResultadoPaginadoDTO<UsuarioDTO> getUsuarios(FiltroUsuarioDTO filtro) throws PDIException {
        List<UsuarioDTO> listaUsuarios = new ArrayList<>(this.mapUsuarios.values());

        String login = filtro.getLogin();
        String nombre = filtro.getNombre();
        String apellido = filtro.getApellido();

        listaUsuarios = ListaUtil.<UsuarioDTO, String>filterList("login", login, listaUsuarios, new StringAcceptor());
        listaUsuarios = ListaUtil.<UsuarioDTO, String>filterList("nombre", nombre, listaUsuarios, new StringAcceptor());
        listaUsuarios = ListaUtil.<UsuarioDTO, String>filterList("apellido", apellido, listaUsuarios, new StringAcceptor());

        long totalTuplas = listaUsuarios.size();

        Collections.sort(listaUsuarios);

        listaUsuarios = ListaUtil.<UsuarioDTO>getSubList(listaUsuarios, filtro.getCurrentPage(), filtro.getPageSize());

        ResultadoPaginadoDTO<UsuarioDTO> resultado = new ResultadoPaginadoDTO<UsuarioDTO>(listaUsuarios);
        resultado.setTotalTuplas(totalTuplas);

        return resultado;
    }

    @Override
    public UsuarioDTO getUsuario(String login) throws PDIException {
        return mapUsuarios.get(login);
    }

    @Override
    public void insertarUsuario(UsuarioDTO usuario) throws PDIException {
        try {
            this.mapUsuarios.put(usuario.getLogin(), usuario);

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(gitManager.getAbsoluteLocalPath(fileUsuarios)), new ArrayList<>(this.mapUsuarios.values()));

            this.gitManager.enviarCambios("Se inserta el usuario con login: " + usuario.getLogin(), fileUsuarios);

        } catch (PDIException e) {
            logger.error("Ha ocurrido un error insertando un nuevo usuario: " + usuario.toString(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Ha ocurrido un error insertando un nuevo usuario: " + usuario.toString(), e);
            throw new PDIException("Ha ocurrido un error insertando un nuevo usuario: " + usuario.toString(), e);
        }
    }

    @Override
    public void eliminarUsuario(String login) throws PDIException {
        try {
            this.mapUsuarios.remove(login);

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(gitManager.getAbsoluteLocalPath(fileUsuarios)), new ArrayList<>(this.mapUsuarios.values()));

            this.gitManager.enviarCambios("Se elimina el usuario con login: " + login, fileUsuarios);

        } catch (PDIException e) {
            logger.error("Ha ocurrido un error eliminando el usuario con login: " + login, e);
            throw e;
        } catch (Exception e) {
            logger.error("Ha ocurrido un error eliminando el usuario con login: " + login, e);
            throw new PDIException("Ha ocurrido un error eliminando el usuario con login: " + login, e);
        }
    }

    @ManagedOperation
    public void clonarRepositorio() {
        try {
            this.gitManager.clonarRepositorio();
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error clonando el repositorio");
        }
    }

    @ManagedOperation
    public String getUsuarios() {
        StringBuffer buffer = new StringBuffer();
        for (UsuarioDTO item : this.mapUsuarios.values()) {
            buffer.append(item.toString());
            buffer.append(", \n");
        }
        return buffer.toString();
    }


    @Override
    public void limpiarCache() {
        init();
    }

}
