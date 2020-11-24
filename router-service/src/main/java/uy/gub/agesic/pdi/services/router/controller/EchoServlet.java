package uy.gub.agesic.pdi.services.router.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

@Service
public class EchoServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(EchoServlet.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long start = System.currentTimeMillis();
            InputStream is = req.getInputStream();
            byte[] bytes = IOUtils.toByteArray(is);
            resp.setContentType(req.getContentType());
            resp.setStatus(200);

            if (bytes != null) {
                resp.getOutputStream().write(bytes);
            }

            Enumeration<String> headers = req.getHeaderNames();
            while (headers.hasMoreElements()) {
                String headerName = headers.nextElement();
                String headerValue = req.getHeader(headerName);
                resp.setHeader(headerName, headerValue);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
