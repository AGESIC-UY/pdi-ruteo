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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.backoffice.dtos.FiltroRutaDTO;
import uy.gub.agesic.pdi.backoffice.dtos.RutaDTO;
import uy.gub.agesic.pdi.backoffice.integration.RouteDataService;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.soap.DataUtil;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import java.net.URI;
import java.util.List;

@Service
public class RutaServiceImpl implements RutaService {

    private RouteDataService routeDataService;

    private DiscoveryClient discoveryClient;

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static Logger logger = LoggerFactory.getLogger(RutaServiceImpl.class);

    @Autowired
    public RutaServiceImpl(RouteDataService routeDataService, DiscoveryClient discoveryClient) {
        this.routeDataService = routeDataService;
        this.discoveryClient = discoveryClient;
    }

    @Override
    @Loggable
    public void eliminarRutas(List<String> logicals) throws BackofficeException {
        for (String logical : logicals) {
            try {
                String encoded = DataUtil.encode(logical.getBytes(DEFAULT_ENCODING));
                this.routeDataService.deleteRoute(encoded);
                this.invalidateCache(encoded);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                if (ex.getCause() != null && ex.getCause() instanceof com.netflix.client.ClientException) {
                    throw new BackofficeException("No ha sido posible establecer conexion con los servicios del ruteador", ex);
                } else {
                    throw new BackofficeException("No ha sido posible eliminar las rutas con id logico: " + logical, ex);
                }
            }
        }
    }

    @Override
    @Loggable
    public ResultadoPaginadoDTO<RutaDTO> buscarRutas(FiltroRutaDTO filtro) throws BackofficeException {
        try {
            Canonical<FiltroRutaDTO> filtroMessage = new Canonical<>(filtro);

            ResponseEntity<Canonical<ResultadoPaginadoDTO<RutaDTO>>> response = this.routeDataService.getRoutes(filtroMessage);
            Canonical<ResultadoPaginadoDTO<RutaDTO>> responseMessage = response.getBody();

            ResultadoPaginadoDTO<RutaDTO> resultado = responseMessage.getPayload();

            return resultado;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            if (ex.getCause() != null && ex.getCause() instanceof com.netflix.client.ClientException) {
                throw new BackofficeException("No ha sido posible establecer conexion con los servicios del ruteador", ex);
            } else {
                throw new BackofficeException("No ha sido posible recuperar las rutas indicadas");
            }
        }
    }

    @Override
    @Loggable
    public RutaDTO obtenerRuta(String logical) throws BackofficeException {
        try {

            String encoded = DataUtil.encode(logical.getBytes("UTF-8"));
            ResponseEntity<Canonical<RutaDTO>> response = this.routeDataService.getRoute(encoded);
            Canonical<RutaDTO> ruta = response.getBody();

            return ruta.getPayload();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            if (ex.getCause() != null && ex.getCause() instanceof com.netflix.client.ClientException) {
                throw new BackofficeException("No ha sido posible establecer conexion con los servicios del ruteador", ex);
            } else {
                throw new BackofficeException("No ha sido posible recuperar las rutas con identificador: " + logical);
            }
        }
    }

    @Override
    @Loggable
    public void crearRuta(RutaDTO ruta, String logicalOld) throws BackofficeException {
        try {
            Canonical<RutaDTO> mensaje = new Canonical<>(ruta);

            mensaje.getHeaders().put("logicalOld",logicalOld);

            this.routeDataService.createRoute(mensaje);
            String encoded = DataUtil.encode(ruta.getLogical().getBytes("UTF-8"));
            this.invalidateCache(encoded);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            if (ex.getCause() != null && ex.getCause() instanceof com.netflix.client.ClientException) {
                throw new BackofficeException("No ha sido posible establecer conexion con los servicios del ruteador", ex);
            } else {
                throw new BackofficeException("No ha sido posible crear la ruta con identificador: " + ruta.getLogical());
            }
        }
    }

    @Override
    @Loggable
    public void modificarRuta(RutaDTO ruta, String logicalOld) throws BackofficeException {
        try {
            Canonical<RutaDTO> mensaje = new Canonical<>(ruta);

            mensaje.getHeaders().put("logicalOld",logicalOld);

            this.routeDataService.updateRoute(mensaje);
            String encoded = DataUtil.encode(logicalOld.getBytes("UTF-8"));
            this.invalidateCache(encoded);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            if (ex.getCause() != null && ex.getCause() instanceof com.netflix.client.ClientException) {
                throw new BackofficeException("No ha sido posible establecer conexion con los servicios del ruteador", ex);
            } else {
                throw new BackofficeException("No ha sido posible actualizar la ruta con identificador: " + ruta.getLogical());
            }
        }
    }

    @Loggable
    private void invalidateCache(String logical) throws Exception {
        List<String> services = this.discoveryClient.getServices();

        for (String service : services) {
            if (service.equalsIgnoreCase("router-service")) {
                List<ServiceInstance> instances = this.discoveryClient.getInstances(service);

                for (ServiceInstance s : instances) {

                    String ipAddress = ((EurekaDiscoveryClient.EurekaServiceInstance) s).getInstanceInfo().getIPAddr();
                    int port = ((EurekaDiscoveryClient.EurekaServiceInstance) s).getInstanceInfo().getPort();

                    String scheme = (s.isSecure()) ? "https" : "http";
                    String uri = String.format("%s://%s:%s", scheme, ipAddress, port);

                    String routeServiceUrl = URI.create(uri).toString();

                    RouteDataService rdService = Feign.builder()
                            .encoder(new GsonEncoder())
                            .decoder(new GsonDecoder())
                            .contract(new SpringMvcContract())
                            .target(RouteDataService.class, routeServiceUrl);
                    rdService.deleteRouteFromCache(logical);
                }
            }
        }
    }
}
