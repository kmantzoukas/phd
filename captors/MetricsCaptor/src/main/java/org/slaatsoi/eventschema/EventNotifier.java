//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.24 at 12:58:26 PM BST 
//


package org.slaatsoi.eventschema;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EventNotifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventNotifier">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IP" type="{http://www.slaatsoi.org/eventschema}ipAddress"/>
 *         &lt;element name="Port" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventNotifier", propOrder = {
    "name",
    "ip",
    "port"
})
public class EventNotifier
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "IP", required = true)
    protected String ip;
    @XmlElement(name = "Port")
    protected long port;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the ip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIP() {
        return ip;
    }

    /**
     * Sets the value of the ip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIP(String value) {
        this.ip = value;
    }

    /**
     * Gets the value of the port property.
     * 
     */
    public long getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     */
    public void setPort(long value) {
        this.port = value;
    }

}