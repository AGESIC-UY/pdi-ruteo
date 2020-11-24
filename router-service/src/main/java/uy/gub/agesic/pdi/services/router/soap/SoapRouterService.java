package uy.gub.agesic.pdi.services.router.soap;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.exceptions.PDIException;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.logging.PDIHostName;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.canonical.Error;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.soap.BodyProcessor;
import uy.gub.agesic.pdi.common.utiles.CanonicalProcessor;
import uy.gub.agesic.pdi.services.httpproxy.business.ErrorProcessor;
import uy.gub.agesic.pdi.services.httpproxy.business.WebProxyBusiness;
import uy.gub.agesic.pdi.services.router.access.AccessManager;
import uy.gub.agesic.pdi.services.router.access.AccessToken;
import uy.gub.agesic.pdi.services.router.config.RouterProperties;
import uy.gub.agesic.pdi.services.router.domain.RutaDTO;
import uy.gub.agesic.pdi.services.router.exceptions.SoapRouterException;
import uy.gub.agesic.pdi.services.router.service.RouteDataService;
import uy.gub.agesic.pdi.services.router.util.Constants;

import java.net.UnknownHostException;

@Component
public class SoapRouterService {

    private static final Logger logger = LoggerFactory.getLogger(SoapRouterService.class);

    private WebProxyBusiness webProxyBusiness;
    private RouteDataService routeService;
    private WsaInspector wsaInspector;
    private SoapTransformer soapTransformer;
    private MTOMProcessor mtomProcessor;
    private SoapErrorProcessor soapErrorProcessor;
    private AccessManager accessManager;
    private RouterProperties routerProperties;
    private ErrorProcessor errorProcessor;

    @Autowired
    public SoapRouterService(WebProxyBusiness webProxyBusiness, RouteDataService routeService, WsaInspector wsaInspector, SoapTransformer soapTransformer,
                             SoapErrorProcessor soapErrorProcessor, MTOMProcessor mtomProcessor, AccessManager accessManager, RouterProperties routerProperties, ErrorProcessor errorProcessor) {

        this.webProxyBusiness = webProxyBusiness;
        this.routeService = routeService;
        this.wsaInspector = wsaInspector;
        this.soapTransformer = soapTransformer;
        this.soapErrorProcessor = soapErrorProcessor;
        this.mtomProcessor = mtomProcessor;
        this.accessManager = accessManager;
        this.routerProperties = routerProperties;
        this.errorProcessor = errorProcessor;
    }

    @HystrixCommand(commandKey = "ProcessSoap", threadPoolKey = "ProcessSoap", fallbackMethod = "processSoapFallback")
    @Loggable
    public Canonical<SoapPayload> processSoap(Canonical<SoapPayload> message) {
        String transactionId = message.getHeaders().get(Constants.TRANSACTIONID_HEADER_NAME).toString();
        MDC.put(Constants.TRANSACTIONID_HEADER_NAME, transactionId);
        MDC.put("host", PDIHostName.HOST_NAME);

        // Preprocesamos el mensaje para cargar los headers en el mismo. Esto inicializa el ID de transaccion del mensaje
        try {
            message = preprocessSoap(message);
        } catch (Exception e) {
            Error error = soapErrorProcessor.createError(new SoapRouterException(e));
            message.getHeaders().put(Constants.ERROR_HEADER_NAME, error);
            logger.error(Constants.ERRORPROCSOAP + " " + errorProcessor.getDescriptionByCode(Constants.ERRORPROCSOAP), e);
        } finally {
            soapErrorProcessor.processErrors(message);
        }

        // Si hubo error, nos vamos
        if ("500".equalsIgnoreCase(message.getPayload().getResponseStatusCode())) {
            return message;
        }

        // Recuperamos el header de addressing que indica la ruta logica

        String wsaTo = wsaInspector.getWsaTo(message);

        // Pedimos acceso al circuit breaker para invocar al sevicio
        // Si estamos en modo no degradado, la llamada es una llamada normal
        // Si estamos en modo degradado, entonces la llamada es controlada, y/o potencialmente rechazada

        try (AccessToken accessToken = this.accessManager.requestAccess(wsaTo)) {

            String xml = getPayload(message);
            String wsaAction = wsaInspector.getWsaAction(message);

            String cs = CanonicalProcessor.getCharSet(message);
            xml = soapTransformer.changeMustunderstand(xml, cs, BodyProcessor.isEmptyBody(message));
            xml = soapTransformer.changeWsaTo(xml, (String) message.getHeaders().get(Constants.PHYSICALURL_HEADER_NAME), cs, BodyProcessor.isEmptyBody(message));
            message.getPayload().setBase64Data(CanonicalProcessor.encodeData(xml, cs));

            mtomProcessor.restoreMTOM(message);

            // Colocamos el timeout definido en la propiedad routerTimeout
            int timeOut = this.routerProperties.getRouterTimeout();
            message.getHeaders().put(Constants.TIMEOUT_HEADER_NAME, timeOut);

            // Invocamos el servicio
            Canonical<SoapPayload> responseMessage = webProxyBusiness.invokeEndpoint(message);

            String serviceTimestamp = (String) responseMessage.getHeaders().get(Constants.SERVICETIMESTAMP_HEADER_NAME);
            long end = Long.parseLong(serviceTimestamp != null ? serviceTimestamp : "0");

            // Verificamos si nos degradamos
            accessToken.checkDegradation(end);

            // Procesamos la respuesta
            mtomProcessor.saveMTOM(responseMessage);

            processResponse(responseMessage, message, wsaAction);

            mtomProcessor.restoreMTOM(message);

        } catch (SoapRouterException e) {
            Error error = soapErrorProcessor.createError(e);
            message.getHeaders().put(Constants.ERROR_HEADER_NAME, error);
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            Error error = soapErrorProcessor.createError(new SoapRouterException(e));
            message.getHeaders().put(Constants.ERROR_HEADER_NAME, error);
            logger.error(e.getMessage(), e);
        } finally {
            soapErrorProcessor.processErrors(message);
        }

        return message;
    }

    private Canonical<SoapPayload> preprocessSoap(Canonical<SoapPayload> message) throws Exception {
        long start = System.currentTimeMillis();
        message.getHeaders().put(Constants.STARTEDAT_HEADER_NAME, "" + start);

        mtomProcessor.saveMTOM(message);

        String xml = getPayload(message);

        BodyProcessor.processSoap(message, xml);

        wsaInspector.processHeaders(message, xml);
        String wsaTo = wsaInspector.getWsaTo(message);
        MDC.put(Constants.MESSAGEID_MDC_PARAM_NAME, wsaInspector.getWsaMessageID(message));
        MDC.put(Constants.LOGICALURI_MDC_PARAM_NAME, wsaTo);
        message.getHeaders().put(Constants.SERVICENAME_HEADER_NAME, wsaTo);

        // Cargamos las rutas desde el cache
        mapService(message);

        return message;
    }

    private Canonical<SoapPayload> processSoapFallback(Canonical<SoapPayload> message, Throwable e) {
        try {
            logger.error(e.getMessage(), e);
            Error error = soapErrorProcessor.createError(new SoapRouterException(e));
            message.getHeaders().put(Constants.ERROR_HEADER_NAME, error);
            soapErrorProcessor.processErrors(message);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return message;
    }

    @Loggable
    private void processResponse(Canonical<SoapPayload> responseMessage, Canonical<SoapPayload> message, String originalWsaAction) throws Exception {
        message.setPayload(responseMessage.getPayload());
        message.getHeaders().put(Constants.ERROR_HEADER_NAME, responseMessage.getHeaders().get(Constants.ERROR_HEADER_NAME));
        message.getHeaders().put(Constants.SERVICETIMESTAMP_HEADER_NAME, responseMessage.getHeaders().get(Constants.SERVICETIMESTAMP_HEADER_NAME));
        message.getHeaders().put(Constants.WEBPROXYTIMESTAMP_HEADER_NAME, responseMessage.getHeaders().get(Constants.WEBPROXYTIMESTAMP_HEADER_NAME));

        String contentType = message.getPayload().getContentType();

        boolean isXMLResponse = false;
        if (contentType != null) {
            isXMLResponse = contentType.toLowerCase().lastIndexOf("xml") > 0;
        }

        if (!message.getPayload().getResponseStatusCode().equalsIgnoreCase("500") && message.getPayload().getBase64Data() != null && isXMLResponse) {
            String xml = getPayload(message);
            BodyProcessor.processSoap(message, xml);
            String cs = CanonicalProcessor.getCharSet(message);
            xml = soapTransformer.changeWsaActionsResponse(xml, originalWsaAction, cs, BodyProcessor.isEmptyBody(message));
            message.getPayload().setBase64Data(CanonicalProcessor.encodeData(xml, cs));
        } else if (!isXMLResponse && message.getPayload().getDataMTOM() == null) {
            logger.warn("Formato de respuesta desconocido o no es un xml valido: " + contentType);
            logger.warn("Message: " + message.toString());
            throw new SoapRouterException("Formato de respuesta desconocido", null, Constants.CONTENT_TYPE_ERROR, null);
        }

    }

    private String getPayload(Canonical<SoapPayload> message) throws SoapRouterException {
        try {
            return CanonicalProcessor.decodeSoap(message.getPayload());
        } catch (Exception e) {
            throw new SoapRouterException("Error interno", null, Constants.ERRORDECODESOAP, e);
        }
    }

    @Loggable
    private void mapService(Canonical<SoapPayload> message) throws SoapRouterException, PDIException {
        String wsaTo = wsaInspector.getWsaTo(message);
        RutaDTO route = null;
        if (wsaTo != null) {
            route = routeService.obtenerRuta(wsaTo);
        }

        if (route == null) {
            throw new SoapRouterException("No existe ruta f\u00EDsica para la ruta l\u00F3gica " + wsaTo, null, Constants.RUTANOENCONTRADA, null);
        }

        // Sacamos los datos de la ruta fisica
        String url = route.getPhysical().trim();
        message.getHeaders().put(Constants.PHYSICALURL_HEADER_NAME, url);
        String baseURL = route.getBaseURI() != null ? route.getBaseURI().trim() : null;
        if (baseURL != null && baseURL.length() > 0) {
            String routePhysical = baseURL;
            if (routePhysical.endsWith("/")) {
                routePhysical = routePhysical.substring(0, routePhysical.length() - 1);
            }
            int index = url.lastIndexOf("://");
            if (index != -1) {
                String endpointAux = url.substring(index + 3);
                index = endpointAux.indexOf("/");
                if (index != -1) {
                    routePhysical = routePhysical + endpointAux.substring(index);
                }
            }
            url = routePhysical;
        }

        message.getHeaders().put(Constants.URL_HEADER_NAME, url);
    }

}


