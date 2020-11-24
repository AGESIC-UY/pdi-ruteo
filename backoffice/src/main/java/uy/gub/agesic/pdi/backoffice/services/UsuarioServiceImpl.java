package uy.gub.agesic.pdi.backoffice.services;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.backoffice.dtos.FiltroUsuarioDTO;
import uy.gub.agesic.pdi.backoffice.dtos.UsuarioDTO;
import uy.gub.agesic.pdi.backoffice.integration.UsuarioDataService;
import uy.gub.agesic.pdi.backoffice.repository.UsuarioRepository;
import uy.gub.agesic.pdi.backoffice.utiles.crypto.JasyptUtil;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import java.net.URI;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private UsuarioRepository usuarioRepository;

    private DiscoveryClient discoveryClient;


    @Autowired
    public UsuarioServiceImpl (UsuarioRepository usuarioRepository, DiscoveryClient discoveryClient ) {
        this.usuarioRepository = usuarioRepository;
        this.discoveryClient = discoveryClient;
    }

    @Override
    @Loggable
    public boolean authenticate(String username, String password) throws BackofficeException {
        try {
            UsuarioDTO usuario = usuarioRepository.getUsuario(username);

            return JasyptUtil.matchesPassword(password, usuario.getPassword());
        } catch (Exception ex) {
            logger.error("Ha ocurrido un error autenticando al usuario: " + username, ex);
            throw new BackofficeException("Credenciales invalidas");
        }
    }

    @Override
    @Loggable
    public ResultadoPaginadoDTO<UsuarioDTO> buscarUsuarios(FiltroUsuarioDTO filtro) throws BackofficeException {
        try {
            return usuarioRepository.getUsuarios(filtro);
        } catch (Exception ex) {
            throw new BackofficeException("No ha sido posible recuperar los usuarios indicados");
        }
    }

    @Override
    @Loggable
    public UsuarioDTO obtenerUsuario(String login) throws BackofficeException {
        try {
            UsuarioDTO response = usuarioRepository.getUsuario(login);
            return response;
        } catch (Exception ex) {
            throw new BackofficeException("No ha sido posible obtener el usuario con login: " + login);
        }
    }

    @Override
    @Loggable
    public void crearUsuario(UsuarioDTO usuario) throws BackofficeException {
        try {
            String hashPassword = JasyptUtil.hashPassword(usuario.getPassword());
            usuario.setPassword(hashPassword);

            usuarioRepository.insertarUsuario(usuario);
            this.invalidateCache();
        } catch (Exception ex) {
            throw new BackofficeException("No ha sido posible crear el usuario: " + usuario.getLogin());
        }
    }

    @Override
    @Loggable
    public void modificarUsuario(UsuarioDTO usuario) throws BackofficeException {
        try {
            usuarioRepository.insertarUsuario(usuario);
            this.invalidateCache();
        } catch (Exception ex) {
            throw new BackofficeException("No ha sido posible actualizar el usuario: " + usuario.getLogin());
        }
    }

    @Override
    @Loggable
    public boolean cambiarContrasena(String login, String oldPassword, String newPassword) throws BackofficeException {
        boolean success = false;

        try {
            UsuarioDTO usuario = usuarioRepository.getUsuario(login);

            if (JasyptUtil.matchesPassword(oldPassword, usuario.getPassword())){
                String hashPassword = JasyptUtil.hashPassword(newPassword);
                usuario.setPassword(hashPassword);
                usuarioRepository.insertarUsuario(usuario);
                this.invalidateCache();
                success = true;
            }
        } catch (Exception ex) {
            logger.error("Ha ocurrido un error al modificar la contraseña del usuario: " + login, ex);
            throw new BackofficeException("Error al modificar conraseña");
        }

        return success;
    }

    @Override
    @Loggable
    public String permisoUsuario(String login) throws BackofficeException {
        try {
            UsuarioDTO usuario = usuarioRepository.getUsuario(login);
            return usuario.getPermiso();
        } catch (Exception ex) {
            logger.error("Ha ocurrido un error al obtener el nivel de permiso del usuario: " + login, ex);
            throw new BackofficeException("Error al obtener permiso");
        }
    }

    @Override
    @Loggable
    public void eliminarUsuarios (List<String> logins) throws BackofficeException {
        for (String login : logins) {
            try {
                usuarioRepository.eliminarUsuario(login);
                this.invalidateCache();
            } catch (Exception ex) {
                throw new BackofficeException("No ha sido posible eliminar el usuario: " + login);
            }
        }
    }

    @Loggable
    private void invalidateCache(){

        List<String> services = this.discoveryClient.getServices();

        for (String service : services) {
            if (service.equalsIgnoreCase("backoffice")) {
                List<ServiceInstance> instances = this.discoveryClient.getInstances(service);

                for (ServiceInstance s : instances) {

                    String ipAddress = ((EurekaDiscoveryClient.EurekaServiceInstance) s).getInstanceInfo().getIPAddr();
                    int port = ((EurekaDiscoveryClient.EurekaServiceInstance) s).getInstanceInfo().getPort();

                    String scheme = (s.isSecure()) ? "https" : "http";
                    String uri = String.format("%s://%s:%s", scheme, ipAddress, port);

                    String usuarioServiceUrl = URI.create(uri).toString();

                    UsuarioDataService rdService = Feign.builder()
                            .encoder(new GsonEncoder())
                            .decoder(new GsonDecoder())
                            .contract(new SpringMvcContract())
                            .target(UsuarioDataService.class, usuarioServiceUrl);
                    rdService.deleteUsuariosFromCache();
                }
            }
        }

    }

}