package uy.gub.agesic.pdi.services.router.soap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.soap.DataUtil;
import uy.gub.agesic.pdi.common.utiles.HttpUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Enumeration;

@Component
public class MTOMProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MTOMProcessor.class);

    @Loggable
    void saveMTOM(Canonical<SoapPayload> message) throws Exception {
        SoapPayload payload = message.getPayload();
        String dataEncoded = payload.getBase64Data();

        if (dataEncoded != null && payload.getContentType().toLowerCase().contains("multipart")) {
            byte[] dataMTOM = DataUtil.decode(dataEncoded);
            String mtomContentType = payload.getContentType();
            message.getPayload().setDataMTOM(dataMTOM);
            message.getPayload().setMtomContentType(mtomContentType);
            final String contentType = mtomContentType;
            final byte[] data = dataMTOM;
            MimeMultipart m = new MimeMultipart(new DataSource() {

                public OutputStream getOutputStream() throws IOException {
                    throw new IOException("not supported");
                }

                public String getName() {
                    return "name";
                }

                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(data);
                }

                public String getContentType() {
                    return contentType;
                }
            });

            if (m.getBodyPart(0) != null) {
                Object content = m.getBodyPart(0).getContent();
                String ct = m.getBodyPart(0).getContentType();
                String cs = HttpUtil.getCharsetFromContentType(m.getBodyPart(0).getContentType());
                if (content instanceof StreamSource || content instanceof InputStream) {
                    InputStream is;
                    if (content instanceof StreamSource) {
                        StreamSource source = (StreamSource) m.getBodyPart(0).getContent();
                        is = source.getInputStream();
                    } else {
                        is = (InputStream) content;
                    }
                    if (is != null) {
                        byte[] bytes = IOUtils.toByteArray(is);
                        String encoded = DataUtil.encode(bytes);
                        message.getPayload().setBase64Data(encoded);
                        message.getPayload().setContentType(ct);
                    }
                } else if (content instanceof String) {
                    byte[] bytes = ((String) content).getBytes(cs);
                    String encoded = DataUtil.encode(bytes);
                    message.getPayload().setBase64Data(encoded);
                    message.getPayload().setContentType(ct);
                }
            }
        }
    }

    @Loggable
    void restoreMTOM(Canonical<SoapPayload> message) throws Exception {
        if (message.getPayload().getDataMTOM() != null) {
            byte[] dataMTOM = message.getPayload().getDataMTOM();
            String contentType = message.getPayload().getMtomContentType();
            MimeMultipart m = new MimeMultipart(new BodyDataSource(dataMTOM, contentType));

            BodyPart soapPart = m.getBodyPart(0);
            m.removeBodyPart(0);

            String ct = soapPart.getContentType();
            String body = message.getPayload().getBase64Data();
            byte[] soapData = DataUtil.decode(body);
            DataHandler dh2 = new DataHandler(new BodyDataSource(soapData, ct));
            MimeBodyPart mimePart = new MimeBodyPart();
            mimePart.setDataHandler(dh2);

            Enumeration enumeration = soapPart.getAllHeaders();
            while (enumeration.hasMoreElements()) {
                Header h = (Header) enumeration.nextElement();
                mimePart.setHeader(h.getName(), h.getValue());
            }
            m.addBodyPart(mimePart, 0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            m.writeTo(stream);

            message.getPayload().setMtomContentType(null);
            message.getPayload().setDataMTOM(null);

            message.getPayload().setBase64Data(DataUtil.encode(stream.toByteArray()));
            message.getPayload().setContentType(contentType);
        }
    }

    private class BodyDataSource implements DataSource {

        private byte[] data;
        private String contentType;

        public BodyDataSource(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("not supported");
        }

        public String getName() {
            return "name";
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getContentType() {
            return contentType;
        }

    }
}
