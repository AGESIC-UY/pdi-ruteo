package uy.gub.agesic.pdi.services.router.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.services.router.config.RouterProperties;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;
import uy.gub.agesic.pdi.services.router.exceptions.SoapRouterException;
import uy.gub.agesic.pdi.services.router.service.RouteDataService;
import uy.gub.agesic.pdi.services.router.util.Constants;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Component
@ManagedResource
@RefreshScope
public class AccessManager {

    private static final Logger logger = LoggerFactory.getLogger(AccessManager.class);

    private RouteDataService routeDataService;

    private RouterProperties routerProperties;

    private Map<String, SemaphoreData> routeSemaphores;

    private Map<String, RouteStatus> routeStatus;

    @Value("${degradation.count:10}")
    private int countData = 10;

    @Autowired
    public AccessManager(RouteDataService routeDataService, RouterProperties routerProperties) {
        this.routeDataService = routeDataService;
        this.routerProperties = routerProperties;

        this.routeSemaphores = new HashMap<>();

        this.routeStatus = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        try {
            loadData();
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error inicializando las rutas desde el repositorio de rutas", e);
        }
    }

    private void loadData() throws PDIException {
        List<RutaDTO> rutas = this.routeDataService.obtenerTodasLasRutas();
        this.routeSemaphores.clear();
        this.routeStatus.clear();
        for (RutaDTO ruta : rutas) {
            this.routeSemaphores.put(ruta.getLogical().trim(), new SemaphoreData(ruta.getDegradePermits() == null ? 0 : ruta.getDegradePermits()));
            this.routeStatus.put(ruta.getLogical().trim(), new RouteStatus(this.countData, ruta.getDegraded() == null ? false : ruta.getDegraded()));
        }
    }

    private RouteStatus getRouteStatus(RutaDTO ruta, String logical) {
        RouteStatus rs = this.routeStatus.get(logical);
        SemaphoreData semData = this.routeSemaphores.get(logical);
        if (rs == null || rs.isDegradable() != ruta.getDegraded().booleanValue()) {
            if (semData == null) {
                semData = createSemaphoreData(ruta);
                this.routeSemaphores.put(ruta.getLogical().trim(), semData);
            }

            rs = new RouteStatus(this.countData, ruta.getDegraded() == null ? false : ruta.getDegraded());
            this.routeStatus.put(ruta.getLogical().trim(), rs);
        }

        //Si se cambia la cantidad desde el backoffice creo un nuevo semaforo
        if (semData.getPermits() != (ruta.getDegradePermits() == null ? 0 : ruta.getDegradePermits().intValue())) {
            semData = createSemaphoreData(ruta);
            this.routeSemaphores.put(ruta.getLogical().trim(), semData);
        }

        rs.setDegradable(ruta.getDegraded());
        return rs;
    }

    private SemaphoreData createSemaphoreData(RutaDTO ruta) {
        return new SemaphoreData(ruta.getDegradePermits() == null ? 0 : ruta.getDegradePermits());
    }

    @Loggable
    public AccessToken requestAccess(String logical) throws SoapRouterException, PDIException {
        RutaDTO route = this.routeDataService.obtenerRuta(logical);
        if (route == null) {
            throw new SoapRouterException("No se encuentra la ruta f\u00EDsica. Ruta l\u00F3gica: " + logical, null, Constants.RUTANOENCONTRADA, null);
        }

        RouteStatus rs = getRouteStatus(route, route.getLogical().trim());
        // Si la ruta no esta degradada, entonces generamos un access token vacio.
        // Si el access manager no esta habilitdo, entonces tambien generamos un access token vacio
        if (!this.routerProperties.getAccessManagerEnabled() || route.getDegraded() == null || !route.getDegraded()) {
            return new AccessToken(this, route, rs, false);
        }

        if (!rs.isDegraded()) {
            return new AccessToken(this, route, rs, false);
        }

        // Si la ruta esta degradada, entonces vemos si podemos acceder. Verificamos la cantidad de permisos
        // disponibles para la ruta y luego pedimos permiso al semaforo correspondiente.

        SemaphoreData semaphoreData = this.routeSemaphores.get(route.getLogical().trim());
        if (semaphoreData == null) {
            throw new SoapRouterException("Ruta degradada sin sem\u00E1foro. Ruta l\u00F3gica: " + logical, null, Constants.RUTADEGNOSEMAPHORE, null);
        }

        Semaphore semaphore = semaphoreData.getSemaphore();
        // Obtenemos un permiso, si no lo hay, nos vamos con error

        if (!semaphore.tryAcquire()) {
            throw new SoapRouterException("Ruta degradada sin permitidos. Ruta l\u00F3gica: " + logical, null, Constants.RUTADEGNOPERMITS, null);
        }

        // En este punto tenemos permiso, nos vamos con el acceso

        return new AccessToken(this, route, rs, true);
    }

    @Loggable
    public void releaseAccess(String logical) throws SoapRouterException {
        SemaphoreData semaphoreData = this.routeSemaphores.get(logical);
        if (semaphoreData == null) {
            throw new SoapRouterException("Ruta degradada sin sem\u00E1foro. Ruta l\u00F3gica: " + logical, null, Constants.RUTADEGNOSEMAPHORE, null);
        }
        Semaphore semaphore = semaphoreData.getSemaphore();
        semaphore.release();
    }

    @ManagedOperation
    public String getRouteStatus() {
        try {
            List<RutaDTO> rutas = this.routeDataService.obtenerTodasLasRutas();
            StringBuffer buffer = new StringBuffer();
            for (RutaDTO ruta : rutas) {
                String logical = ruta.getLogical().trim();
                RouteStatus rs = getRouteStatus(ruta, logical);
                buffer.append("Key: ");
                buffer.append(logical);
                buffer.append(", Value: ");
                buffer.append(rs.toString());
                buffer.append(", \n");
            }
            return buffer.toString();
        } catch (PDIException e) {
            String msg = "Ha ocurrido un error recargando las rutas desde el repositorio de rutas";
            logger.error(msg, e);
            return msg;
        }
    }

    @ManagedOperation
    public String getDegradedRoutes() {
        try {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> iter = routeStatus.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                RutaDTO route = this.routeDataService.obtenerRuta(key.trim());
                if (route.getDegraded()) {
                    RouteStatus rs = routeStatus.get(key);
                    if (rs.isDegraded()) {
                        buffer.append("RutaDTO: ");
                        buffer.append(key);
                        buffer.append(", \n");
                    }
                }
            }
            return buffer.toString();
        } catch (PDIException e) {
            String msg = "Ha ocurrido un error recargando las rutas desde el repositorio de rutas";
            logger.error(msg, e);
            return msg;
        }
    }

    @ManagedAttribute
    public int getCountData() {
        return countData;
    }

    @ManagedAttribute
    public Boolean getAccessManagerEnabled() {
        return routerProperties.getAccessManagerEnabled();
    }

    @ManagedOperation
    public void reLoadData() {
        try {
            loadData();
        } catch (PDIException e) {
            logger.error("Ha ocurrido un error recargando las rutas desde el repositorio de rutas", e);
        }
    }

}
