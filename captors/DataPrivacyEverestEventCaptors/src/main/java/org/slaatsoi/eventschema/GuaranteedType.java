//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.24 at 12:58:26 PM BST 
//


package org.slaatsoi.eventschema;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


/**
 * <p>Java class for GuaranteedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GuaranteedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="guaranteedID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="assessmentResult" type="{http://www.slaatsoi.org/eventschema}AssessmentResultType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GuaranteedType")
@XmlSeeAlso({
    GuatanteedStateType.class
})
public class GuaranteedType
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(required = true)
    protected String guaranteedID;
    @XmlAttribute
    protected AssessmentResultType assessmentResult;

    /**
     * Gets the value of the guaranteedID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuaranteedID() {
        return guaranteedID;
    }

    /**
     * Sets the value of the guaranteedID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuaranteedID(String value) {
        this.guaranteedID = value;
    }

    /**
     * Gets the value of the assessmentResult property.
     * 
     * @return
     *     possible object is
     *     {@link AssessmentResultType }
     *     
     */
    public AssessmentResultType getAssessmentResult() {
        return assessmentResult;
    }

    /**
     * Sets the value of the assessmentResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssessmentResultType }
     *     
     */
    public void setAssessmentResult(AssessmentResultType value) {
        this.assessmentResult = value;
    }

}