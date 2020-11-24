package uy.gub.agesic.pdi.services.router.access;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;

public class AccessToken implements AutoCloseable {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AccessToken.class);

    private AccessManager accessManager;
    private RutaDTO route;
    private Boolean withPermit;
    private RouteStatus routeStatus;

    public AccessToken(AccessManager accessManager, RutaDTO route, RouteStatus routeStatus, Boolean withPermit) {
        this.accessManager = accessManager;
        this.route = route;
        this.withPermit = withPermit;
        this.routeStatus = routeStatus;
    }

    @Override
    public void close() throws Exception {
        if (withPermit) {
            // Si el pedido fue hecho con un permiso porque la ruta esta degradada, entonces debemos retornarlo
            this.accessManager.releaseAccess(route.getLogical());
        }
    }

    public RutaDTO getRoute() {
        return route;
    }

    public void checkDegradation(Long delay) {
        MDC.put("duration", "" + delay);
        logger.info("Tiempo de demora");

        try {
            if (route.getDegraded() != null && route.getDegraded()) {
                boolean isDegraded = routeStatus.isDegraded();
                if (routeStatus.checkDegradation(delay, route.getDegradeTimeout()) != isDegraded) {
                    String message = "Cambia la ruta a " + (isDegraded ? "des-degradada" : "degradada");
                    logger.warn(message);
                }

                if (delay > route.getDegradeTimeout()) {
                    String message = "La ruta con url f\u00EDsica (" + route.getPhysical() + ") demor\u00F3 m\u00E1s de lo esperado";
                    logger.info(message);
                }
            }
        } finally {
            MDC.put("duration", "0");
        }
    }

}
