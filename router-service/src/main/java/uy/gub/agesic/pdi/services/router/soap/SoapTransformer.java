package uy.gub.agesic.pdi.services.router.soap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.message.canonical.Error;
import uy.gub.agesic.pdi.common.utiles.XSLTInformation;
import uy.gub.agesic.pdi.common.utiles.XmlTransformer;
import uy.gub.agesic.pdi.services.router.config.RouterProperties;
import uy.gub.agesic.pdi.services.router.exceptions.SoapRouterException;
import uy.gub.agesic.pdi.services.router.util.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SoapTransformer {

    private static final Logger logger = LoggerFactory.getLogger(SoapTransformer.class);

    private RouterProperties routerProperties;

    @Autowired
    public SoapTransformer(RouterProperties routerProperties) {
        this.routerProperties = routerProperties;
    }

    @Loggable
    public String changeMustunderstand(String xml, String cs, boolean skipCopyBody) throws SoapRouterException {
        XSLTInformation xsltInfo = new XSLTInformation();

        xsltInfo.setName("changeMustUnderstand");
        xsltInfo.setPath(Constants.PATHXSLMUSTUNDERSAND);

        String xmlOrigen = xml;
        try {
            xml = XmlTransformer.xslt(xml, xsltInfo, cs);

            if (this.routerProperties.getCopyBodyEnabled() && !skipCopyBody) {
                xml = mantenerBodyTransformacion(xmlOrigen, xml);
            }

        } catch (Exception e) {
            throw new SoapRouterException("Error interno", null, Constants.ERRORCHANGEMUSTUND, e);
        }

        return xml;
    }

    @Loggable
    public String changeWsaTo(String xml, String url, String cs, boolean skipCopyBody) throws SoapRouterException {
        XSLTInformation xsltInfo = new XSLTInformation();

        xsltInfo.setName("changeWsaTo");
        xsltInfo.setPath(Constants.PATHXSLWSATO);
        xsltInfo.getParameters().put("wsaTo", url);

        String xmlOrigen = xml;
        try {
            xml = XmlTransformer.xslt(xml, xsltInfo, cs);

            if (this.routerProperties.getCopyBodyEnabled() && !skipCopyBody) {
                xml = mantenerBodyTransformacion(xmlOrigen, xml);
            }

        } catch (Exception e) {
            throw new SoapRouterException("Error interno", null, Constants.ERRORCHANGEWSATO, e);
        }

        return xml;
    }

    @Loggable
    public String changeWsaActionsResponse(String xml, String wsaAction, String cs, boolean skipCopyBody) throws SoapRouterException {
        XSLTInformation xsltInfo = new XSLTInformation();

        xsltInfo.setName("addHeader");
        xsltInfo.setPath(Constants.PATHXSLHEADER);
        xsltInfo.getParameters().put("action", wsaAction == null ? "" : wsaAction);

        String xmlOrigen = xml;
        try {
            xml = XmlTransformer.xslt(xml, xsltInfo, cs);

            if (this.routerProperties.getCopyBodyEnabled() && !skipCopyBody) {
                xml = mantenerBodyTransformacion(xmlOrigen, xml);
            }

        } catch (Exception e) {
            throw new SoapRouterException("Error interno", null, Constants.ERRORCHANGEWACTION, e);
        }

        return xml;
    }

    @Loggable
    public String soapFaultTemplate(Error error, String cs) throws SoapRouterException {
        XSLTInformation xsltInfo = new XSLTInformation();
        xsltInfo.setName("soapFault");
        xsltInfo.setPath(Constants.PATHXSLSOAPFAULT);
        xsltInfo.getParameters().put("errorMessage", error.getMessage() == null ? "" : error.getMessage());
        xsltInfo.getParameters().put("errorDescription", error.getDescription() == null ? "" : error.getDescription());
        xsltInfo.getParameters().put("errorCode", error.getCode() == null ? "" : error.getCode());
        xsltInfo.getParameters().put("messageId", error.getMessageId() == null ? "" : error.getMessageId());
        xsltInfo.getParameters().put("relatesTo", error.getRelatesTo() == null ? "" : error.getRelatesTo());
        xsltInfo.getParameters().put("wsaAction", error.getAction() == null ? "" : error.getAction());

        try {
            return XmlTransformer.xslt("<xml/>", xsltInfo, cs);
        } catch (Exception e) {
            throw new SoapRouterException("Error interno", null, Constants.ERRORSOAPFAULTTEMPLATE, e);
        }
    }

    private String mantenerBodyTransformacion(String xmlOrigen, String xmlDestino) {

        String xmlResult = null;

        String regexp = "<(.*:)?body[^>]*>(.*)</\\1?body>";

        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // Buscamos el body original
        Matcher matcherA = pattern.matcher(xmlOrigen);
        Matcher matcherB = pattern.matcher(xmlDestino);

        if (matcherA.find()) {
            // 1 y 3 son los namespaces de los tags en los bodys del mensaje original
            String body = matcherA.group(2);

            if (matcherB.find()) {
                int start = matcherB.start(2);
                int end = matcherB.end(2);

                StringBuilder sb = new StringBuilder(xmlDestino).replace(start, end, body);
                xmlResult = sb.toString();

            }
        }

        return xmlResult;
    }

}
