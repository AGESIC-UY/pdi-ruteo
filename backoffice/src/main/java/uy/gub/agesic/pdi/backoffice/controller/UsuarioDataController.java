package uy.gub.agesic.pdi.backoffice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uy.gub.agesic.pdi.backoffice.repository.UsuarioRepository;

@RestController
public class UsuarioDataController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDataController.class);

    private UsuarioRepository usuarioRepository;


    @Autowired
    public UsuarioDataController (UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }



    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/usuario/cache", method = RequestMethod.DELETE)
    public void deleteUsuariosFromCache() throws Exception{
        try {
            usuarioRepository.limpiarCache();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }
}
