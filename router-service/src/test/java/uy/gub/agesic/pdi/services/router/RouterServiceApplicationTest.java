package uy.gub.agesic.pdi.services.router;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.utiles.CanonicalProcessor;
import uy.gub.agesic.pdi.common.utiles.HttpUtil;
import uy.gub.agesic.pdi.common.utiles.XPathXmlDogUtil;
import uy.gub.agesic.pdi.services.router.config.RouterProperties;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
public class RouterServiceApplicationTest {

    @Test
    public void contextLoads() {
    }

    @Test
    public void checkCanonicalProcessor() {

        String str = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soa=\"http://www.agesic.gub.uy/soa\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
                "   <soapenv:Header>\n" +
                "      <wsa:To soapenv:mustUnderstand=\"1\">http:///www.agesic.gub.uy/Service/Echo</wsa:To>\n" +
                "      <wsa:From>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "      </wsa:From>\n" +
                "      <wsa:ReplyTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">Ping</Example_ns:Parameter2>\n" +
                "            <Parameter1>Message Broker</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:ReplyTo>\n" +
                "      <wsa:FaultTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">FAULT</Example_ns:Parameter2>\n" +
                "            <Parameter1>Ping</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:FaultTo>\n" +
                "      <wsa:MessageID>urn:uuid:020C911C16EB130A8F1204119836321</wsa:MessageID>\n" +
                "      <wsa:Action>http://localhost:7801/Service</wsa:Action>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <soa:Echo>\n" +
                "         <data1>\n" +
                "            <xop:Include href=\"cid:Solicitud de informaci\u00F3n 2_2017 Presentaci\u00F3n de HG v2.pdf\" xmlns:xop=\"http://www.w3.org/2004/08/xop/include\"/>\n" +
                "         </data1>\n" +
                "      </soa:Echo>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        byte[] data = str.getBytes();
        Canonical<SoapPayload> canonical = CanonicalProcessor.createSoapCanonical(data);

        Assert.assertNotNull(canonical);
        Assert.assertNotNull(canonical.getPayload());
        Assert.assertNotNull(canonical.getPayload().getBase64Data());

        data = CanonicalProcessor.getData(canonical);

        String str2 = new String(data);

        Assert.assertTrue("Not equals", str.equals(str2));

    }

    @Test
    public void checkHttpUtil() {
        String cs = HttpUtil.getCharsetFromContentType("multipart/related; type=\"text/xml\"; start=\"<rootpart@soapui.org>\"; boundary=\"----=_Part_2294_687881159.1506629139066\"");
        Assert.assertTrue(cs.equals(HttpUtil.DEFAULT_CHARSET));

        cs = HttpUtil.getCharsetFromContentType("text/xml;charset=ISO-8859-1");
        Assert.assertTrue(cs.equals("ISO-8859-1"));
    }

    @Test
    public void chechTransformer() throws Exception {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soa=\"http://www.agesic.gub.uy/soa\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
                "   <soapenv:Header>\n" +
                "      <wsa:To soapenv:mustUnderstand=\"1\">http:///www.agesic.gub.uy/Service/Echo</wsa:To>\n" +
                "      <wsa:From>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "      </wsa:From>\n" +
                "      <wsa:ReplyTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">Ping</Example_ns:Parameter2>\n" +
                "            <Parameter1>Message Broker</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:ReplyTo>\n" +
                "      <wsa:FaultTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">FAULT</Example_ns:Parameter2>\n" +
                "            <Parameter1>Ping</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:FaultTo>\n" +
                "      <wsa:MessageID>urn:uuid:020C911C16EB130A8F1204119836321</wsa:MessageID>\n" +
                "      <wsa:Action>http://localhost:7801/Service</wsa:Action>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <soa:Echo>\n" +
                "         <data1>\n" +
                "            <xop:Include href=\"cid:Solicitud de informaci\u00F3n 2_2017 Presentaci\u00F3n de HG v2.pdf\" xmlns:xop=\"http://www.w3.org/2004/08/xop/include\"/>\n" +
                "         </data1>\n" +
                "      </soa:Echo>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");

        List<String> xpaths = new LinkedList<>();
        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:To/@soapenv:mustUnderstand");

        List<Object> returnList = XPathXmlDogUtil.executeMultipleXPath(xml, xpaths, namespaces);
        Assert.assertTrue(returnList.size() == 1);
        String mustunderstandValue = (String) returnList.get(0);
        Assert.assertTrue(mustunderstandValue.equals("1"));

        RouterProperties routerProperties = new RouterProperties();
        routerProperties.setAccessManagerEnabled(true);
        routerProperties.setCopyBodyEnabled(true);

//        SoapTransformer transformer = new SoapTransformer(routerProperties);
//
//        String xmlResult = transformer.changeMustunderstand(xml, "UTF-8");
//        Assert.assertNotNull(xmlResult);
//
//        returnList = XPathXmlDogUtil.executeMultipleXPath(xmlResult, xpaths, namespaces);
//        Assert.assertTrue(returnList.size() == 1);
//        mustunderstandValue = (String) returnList.get(0);
//        Assert.assertTrue(mustunderstandValue.equals("0"));
//
//
//        xpaths = new LinkedList<>();
//        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:Action/text()");
//
//        returnList = XPathXmlDogUtil.executeMultipleXPath(xml, xpaths, namespaces);
//        Assert.assertTrue(returnList.size() == 1);
//        String actionValue = (String) returnList.get(0);
//        Assert.assertTrue(actionValue.equals("http://localhost:7801/Service"));
//
//        xmlResult = transformer.changeWsaActionsResponse(xml, null, "UTF-8");
//
//        xmlResult = transformer.changeWsaActionsResponse(xml, "TestAction", "UTF-8");
//        Assert.assertNotNull(xmlResult);
//
//        returnList = XPathXmlDogUtil.executeMultipleXPath(xmlResult, xpaths, namespaces);
//        Assert.assertTrue(returnList.size() == 1);
//        mustunderstandValue = (String) returnList.get(0);
//        Assert.assertTrue(mustunderstandValue.equals("http://localhost:7801/Service"));
//
//        xmlResult = transformer.changeWsaTo(xml, "holaMundo!!", "UTF-8");

    }


    @Ignore
    @Test
    public void modificacionXml() throws Exception {

        String xmlOrigen = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soa=\"http://www.agesic.gub.uy/soa\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
                "   <soapenv:Header>\n" +
                "      <wsa:To soapenv:mustUnderstand=\"1\">http:///www.agesic.gub.uy/Service/Echo</wsa:To>\n" +
                "      <wsa:From>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "      </wsa:From>\n" +
                "      <wsa:ReplyTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">Ping</Example_ns:Parameter2>\n" +
                "            <Parameter1>Message Broker</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:ReplyTo>\n" +
                "      <wsa:FaultTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">FAULT</Example_ns:Parameter2>\n" +
                "            <Parameter1>Ping</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:FaultTo>\n" +
                "      <wsa:MessageID>urn:uuid:020C911C16EB130A8F1204119836321</wsa:MessageID>\n" +
                "      <wsa:Action>http://localhost:7801/Service</wsa:Action>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <soa:GetTimestamp></soa:GetTimestamp>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        String xmlDestino = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:e=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soa=\"http://www.agesic.gub.uy/soa\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
                "   <soapenv:Header>\n" +
                "      <wsa:To soapenv:mustUnderstand=\"1\">http:///www.agesic.gub.uy/Service/Echo</wsa:To>\n" +
                "      <wsa:From>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "      </wsa:From>\n" +
                "      <wsa:ReplyTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">Ping</Example_ns:Parameter2>\n" +
                "            <Parameter1>Message Broker</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:ReplyTo>\n" +
                "      <wsa:FaultTo>\n" +
                "         <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         <wsa:ReferenceParameters>\n" +
                "            <Example_ns:Parameter2 xmlns:Example_ns=\"http://ibm.namespace\">FAULT</Example_ns:Parameter2>\n" +
                "            <Parameter1>Ping</Parameter1>\n" +
                "         </wsa:ReferenceParameters>\n" +
                "      </wsa:FaultTo>\n" +
                "      <wsa:MessageID>urn:uuid:020C911C16EB130A8F1204119836321</wsa:MessageID>\n" +
                "      <wsa:Action>http://localhost:7801/Service</wsa:Action>\n" +
                "   </soapenv:Header>\n" +
                "   <e:body>\n" +
                "      <soa:GetTimestamp/>\n" +
                "   </e:body>\n" +
                "</soapenv:Envelope>";

        String body = null;
        String xmlResult = null;

        String bodyTag = ":body>";
        String xmlLowerCase = xmlOrigen.toLowerCase();
        int inicio = xmlLowerCase.indexOf(bodyTag);
        int index = 0;
        for(int i = inicio; xmlLowerCase.charAt(i) != '<'; i--) {
            index = i;
        }
        inicio = inicio - (inicio -index) - 1;
        int fin = xmlLowerCase.lastIndexOf(bodyTag) + bodyTag.length();
        body =  xmlOrigen.substring(inicio,fin);

        System.out.println("-----------String xmlOriginal \n" + xmlOrigen);
        System.out.println("-----------String xmlDestino \n" + xmlDestino);
        System.out.println("-----------String body \n" + body);

        xmlLowerCase = xmlDestino.toLowerCase();
        int inicio2 = xmlLowerCase.indexOf(bodyTag);
        int fin2 = xmlLowerCase.lastIndexOf(bodyTag) + bodyTag.length();
        int tam = xmlLowerCase.length();

        for(int i = inicio2; xmlLowerCase.charAt(i) != '<'; i--) {
            index = i;
        }
        inicio2 = inicio2 - (inicio2 -index) - 1;
        xmlResult = xmlDestino.substring(0,inicio2) + body + xmlDestino.substring(fin2,tam);

        System.out.println("String xmlResult con body \n" + xmlResult);





    }


}
