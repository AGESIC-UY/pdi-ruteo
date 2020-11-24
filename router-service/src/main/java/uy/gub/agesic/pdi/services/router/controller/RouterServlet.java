package uy.gub.agesic.pdi.services.router.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.common.logging.PDIHostName;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.utiles.CanonicalProcessor;
import uy.gub.agesic.pdi.common.utiles.HttpUtil;
import uy.gub.agesic.pdi.services.router.service.RouterService;
import uy.gub.agesic.pdi.services.router.soap.SoapErrorProcessor;
import uy.gub.agesic.pdi.services.router.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class RouterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RouterServlet.class);

    @Autowired
    private RouterService routerBusiness;

    @Autowired
    private SoapErrorProcessor soapErrorProcessor;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Canonical canonical = null;
        try {

            MDC.clear();

            MDC.put("duration", "0");
            MDC.put("host", PDIHostName.HOST_NAME);
            String transactionId = "uuid:" + UUID.randomUUID();
            MDC.put("transactionId", transactionId);

            InputStream is = req.getInputStream();
            byte[] bytes = IOUtils.toByteArray(is);
            long startTime = System.currentTimeMillis();

            if (logger.isTraceEnabled()) {
                logger.trace("Request payload - " + new String(bytes));
                logger.trace("Request cabezal http - " + HttpUtil.getHeadersRequest(req).toString());
            }

            String contentType = req.getContentType();
            if (contentType.contains("json")) {
                //processJSON
                canonical = routerBusiness.processJson(canonical);
            } else {
                canonical = CanonicalProcessor.createSoapCanonical(bytes);
                canonical.getHeaders().put("initialTime", startTime);

                // Generamos el transaction ID del mensaje
                generateTransactionId(transactionId, canonical);

                bytes = null;
                SoapPayload soapPayload = (SoapPayload) canonical.getPayload();
                soapPayload.setContentType(contentType);

                //processSOAP
                canonical = routerBusiness.processSoap(canonical);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            soapErrorProcessor.processErrors(canonical, e);
        } finally {

            generateResponse(resp, canonical);

        }

    }

    private void generateResponse(HttpServletResponse resp, Canonical canonical) {
        int statusCode = 200;
        byte[] bytes = CanonicalProcessor.getData(canonical);
        if (canonical.getPayload() instanceof SoapPayload) {
            SoapPayload soapPayload = (SoapPayload) canonical.getPayload();
            if (bytes != null) {
                resp.setContentType(soapPayload.getContentType());
            }
            if (soapPayload.getResponseStatusCode() != null) {
                statusCode = Integer.parseInt(soapPayload.getResponseStatusCode());
            }
            Object header = canonical.getHeaders().get("serviceTimestamp");
            resp.setHeader("router_serviceTimestamp", header != null ? header.toString() : "");
            header = canonical.getHeaders().get("webProxyTimestamp");
            resp.setHeader("router_webProxyTimestamp", header != null ? header.toString() : "");
            Long start = (Long) canonical.getHeaders().get("initialTime");
            resp.setHeader("router_responseTime", Long.toString(System.currentTimeMillis() - start));
        }

        resp.setStatus(statusCode);

        canonical.getHeaders().put("endTime", System.currentTimeMillis());
        if (bytes != null) {
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Response payload - " + new String(bytes));
                    logger.trace("Response cabezal http - " + HttpUtil.getHeadersResponse(resp).toString());
                }
                resp.getOutputStream().write(bytes);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void generateTransactionId(String transactionId, Canonical message) {
        message.getHeaders().put(Constants.TRANSACTIONID_HEADER_NAME, transactionId);
    }

}
