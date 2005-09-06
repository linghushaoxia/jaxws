/**
 * $Id: WSHttpHandler.java,v 1.2 2005-09-06 02:57:38 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.WSDLPatcher;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author WS Development Team
 */
public class WSHttpHandler implements HttpHandler {
    
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String XML_CONTENT_TYPE = "text/xml";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
    private static final Localizer localizer = new Localizer();
    private static final LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.httpserver");
    
    private RuntimeEndpointInfo endpointInfo;
    private Tie tie;
    
    public WSHttpHandler(Tie tie, RuntimeEndpointInfo endpointInfo) {
        this.tie = tie;
        this.endpointInfo = endpointInfo;
    }
    
    public void handle(HttpExchange msg) {
        logger.fine("Received HTTP request:"+msg.getRequestURI());
        String method = msg.getRequestMethod();
        if (method.equals(GET_METHOD)) {
            get(msg);
        } else if (method.equals(POST_METHOD)) {
            post(msg);
        } else {
            logger.warning(
                localizer.localize(
                    messageFactory.getMessage(
                        "unexpected.http.method", method)));
            try {
                msg.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();          // Not much can be done
            }
        }
    }
    
    /*
     * Handles POST requests
     */
    private void post(HttpExchange msg) {
        try {
            ServerConnectionImpl con = new ServerConnectionImpl(msg);
            MessageContext msgCtxt = new MessageContextImpl();
            WebServiceContext wsContext = endpointInfo.getWebServiceContext();
            wsContext.setMessageContext(msgCtxt);
            tie.handle(con, endpointInfo);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                msg.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();          // Not much can be done
            }
        }
    }
    
    /*
     * Consumes the entire input stream
     */
    private static void readFully(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        if (is != null) {
            while (is.read(buf) != -1);
        }
    }
    
    /*
     * Handles GET requests
     */ 
    public void get(HttpExchange msg) {
        try {
            WSConnection con = new ServerConnectionImpl(msg);
            InputStream is = con.getInput();
            /*
            try {
                readFully(is);
            } catch(IOException ioe) {
                ioe.printStackTrace();
                String message = "Couldn't read Request";
                writeErrorPage(con, HttpURLConnection.HTTP_INTERNAL_ERROR, message);
                return;
            } finally {
                con.closeInput();
            }
             */
            String queryString = msg.getRequestURI().getQuery();
            logger.fine("Query String for request ="+queryString);

            String inPath = endpointInfo.getPath(queryString);
            if (inPath == null) {
                String message =
                    localizer.localize(
                        messageFactory.getMessage("html.notFound",
                            "Invalid Request ="+msg.getRequestURI()));
                writeErrorPage(con, HttpURLConnection.HTTP_NOT_FOUND, message);
                return;
            }
            DocInfo docInfo = endpointInfo.getDocMetadata().get(inPath);
            if (docInfo == null) {
                String message =
                    localizer.localize(
                        messageFactory.getMessage("html.notFound",
                            "Invalid Request ="+msg.getRequestURI()));
                writeErrorPage(con, HttpURLConnection.HTTP_NOT_FOUND, message);
                return;
            }
            
            InputStream docStream = null;
            try {
                Map<String,List<String>> headers = new HashMap<String, List<String>>();
                List<String> ctHeader = new ArrayList<String>();
                ctHeader.add(XML_CONTENT_TYPE);
                headers.put(CONTENT_TYPE_HEADER, ctHeader);
                con.setHeaders(headers);
                con.setStatus(HttpURLConnection.HTTP_OK);
            /*
                msg.getResponseHeaders().add(CONTENT_TYPE_HEADER, XML_CONTENT_TYPE);
                msg.sendResponseHeaders(HttpURLConnection.HTTP_OK,  0);
             */
                OutputStream os = con.getOutput();

                List<RuntimeEndpointInfo> endpoints = new ArrayList<RuntimeEndpointInfo>();
                endpoints.add(endpointInfo);

                String address =
                    "http"
                        + "://"
                        + msg.getLocalAddress().getHostName()
                        + ":"
                        + msg.getLocalAddress().getPort()
                        + msg.getRequestURI().getPath();
                logger.fine("Address ="+address);
                WSDLPatcher patcher = new WSDLPatcher(docInfo, address,
                        endpointInfo, endpoints);
                docStream = docInfo.getDoc();
                patcher.patchDoc(docStream, os);
            } finally {
                closeInputStream(docStream);
                con.closeOutput();
            }
        } finally {
            try {
                msg.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();          // Not much can be done
            }
        }
    }

    /*
     * writes error html page
     */
    private void writeErrorPage(WSConnection con, int status, String message) {
        try {
            Map<String,List<String>> headers = new HashMap<String, List<String>>();
            List<String> ctHeader = new ArrayList<String>();
            ctHeader.add(HTML_CONTENT_TYPE);
            headers.put(CONTENT_TYPE_HEADER, ctHeader);
            con.setHeaders(headers);
            con.setStatus(status);
            OutputStream outputStream = con.getOutput();
            PrintWriter out = new PrintWriter(outputStream);
            out.println("<html><head><title>");
            out.println(
                localizer.localize(
                    messageFactory.getMessage("html.title")));
            out.println("</title></head><body>");
            out.println(message);
            out.println("</body></html>");
            out.close();
        } finally {
            con.closeOutput();
        }
    }
    
    private static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    private static void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
}
