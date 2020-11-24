package uy.gub.agesic.pdi.backoffice.integration;


import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient("backoffice-service")
public interface UsuarioDataService {

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/usuario/cache", method = RequestMethod.DELETE)
    void deleteUsuariosFromCache();

}
