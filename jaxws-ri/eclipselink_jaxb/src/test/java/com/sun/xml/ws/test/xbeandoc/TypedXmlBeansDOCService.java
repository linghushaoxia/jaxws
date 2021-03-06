
package com.sun.xml.ws.test.xbeandoc;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "TypedXmlBeansDOCService", targetNamespace = "http://www.openuri.org/", wsdlLocation = "file:/D:/TypedXmlBeansDOCService.wsdl")
public class TypedXmlBeansDOCService
    extends Service
{

    private final static URL TYPEDXMLBEANSDOCSERVICE_WSDL_LOCATION;
    private final static WebServiceException TYPEDXMLBEANSDOCSERVICE_EXCEPTION;
    private final static QName TYPEDXMLBEANSDOCSERVICE_QNAME = new QName("http://www.openuri.org/", "TypedXmlBeansDOCService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/D:/TypedXmlBeansDOCService.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        TYPEDXMLBEANSDOCSERVICE_WSDL_LOCATION = url;
        TYPEDXMLBEANSDOCSERVICE_EXCEPTION = e;
    }

    public TypedXmlBeansDOCService() {
        super(__getWsdlLocation(), TYPEDXMLBEANSDOCSERVICE_QNAME);
    }

    public TypedXmlBeansDOCService(WebServiceFeature... features) {
        super(__getWsdlLocation(), TYPEDXMLBEANSDOCSERVICE_QNAME, features);
    }

    public TypedXmlBeansDOCService(URL wsdlLocation) {
        super(wsdlLocation, TYPEDXMLBEANSDOCSERVICE_QNAME);
    }

    public TypedXmlBeansDOCService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, TYPEDXMLBEANSDOCSERVICE_QNAME, features);
    }

    public TypedXmlBeansDOCService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public TypedXmlBeansDOCService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns TypedXmlBeansDOC
     */
    @WebEndpoint(name = "TypedXmlBeansDOCSoapPort")
    public TypedXmlBeansDOC getTypedXmlBeansDOCSoapPort() {
        return super.getPort(new QName("http://www.openuri.org/", "TypedXmlBeansDOCSoapPort"), TypedXmlBeansDOC.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns TypedXmlBeansDOC
     */
    @WebEndpoint(name = "TypedXmlBeansDOCSoapPort")
    public TypedXmlBeansDOC getTypedXmlBeansDOCSoapPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://www.openuri.org/", "TypedXmlBeansDOCSoapPort"), TypedXmlBeansDOC.class, features);
    }

    private static URL __getWsdlLocation() {
        if (TYPEDXMLBEANSDOCSERVICE_EXCEPTION!= null) {
            throw TYPEDXMLBEANSDOCSERVICE_EXCEPTION;
        }
        return TYPEDXMLBEANSDOCSERVICE_WSDL_LOCATION;
    }

}
