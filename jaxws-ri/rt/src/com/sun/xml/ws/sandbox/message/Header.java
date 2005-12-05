package com.sun.xml.ws.sandbox.message;

import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * A SOAP header.
 *
 * <p>
 * A header is read-only, but unlike body it can be read
 * multiple times (TODO: is this really necessary?)
 * The {@link Header} abstraction hides how the header
 * data is represented in memory; instead, it commits to
 * the ability to write itself to XML infoset.
 *
 * <p>
 * When a message is received from the transport and
 * being processed, the processor needs to "peek"
 * some information of a header, such as the tag name,
 * the mustUnderstand attribute, and so on. Therefore,
 * the {@link Header} interface exposes those information
 * as properties, so that they can be checked without
 * replaying the infoset, which is efficiently but still
 * costly.
 *
 * @see HeaderList
 */
public interface Header {
    /**
     * True if this header must be understood.
     */
    public boolean isMustUnderstood();

    /**
     * Gets the value of the soap:actor attribute (or soap:role for SOAP 1.2), or null.
     */
    public String getActor();

    /**
     * Gets the namespace URI of this header element.
     *
     * @return never null
     */
    public String getNamespaceURI();

    /**
     * Gets the local name of this header element.
     *
     * @return never null
     */
    public String getLocalPart();

    /**
     * Reads the header as a {@link XMLStreamReader}
     */
    public XMLStreamReader readHeader();

    /**
     * Reads the header as a JAXB object by using the given unmarshaller.
     *
     */
    public <T> T readAsJAXB(Unmarshaller unmarshaller);
    
    /**
     * Writes out the header.
     */
    public void writeTo(XMLStreamWriterEx w) throws XMLStreamException;

    /**
     * Writes out the header to the given SOAPMessage.
     *
     * TODO: justify why this is necessary
     */
    public void writeTo(SOAPMessage saaj);
}
