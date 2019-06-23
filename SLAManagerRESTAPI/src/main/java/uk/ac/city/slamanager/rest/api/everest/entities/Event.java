package uk.ac.city.slamanager.rest.api.everest.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "event", catalog = "everest")
public class Event {

    private int eventPK;
    private String eventId;
    private BigInteger timestamp;
    private String ecName;
    private String prefix;
    private String operationName;
    private String partnerId;
    private Boolean negated;
    private Boolean abducible;
    private Boolean recordable;
    private Blob eventObject;
    private String eventString;
    private List<EventParameter> parameters = new ArrayList<EventParameter>();

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "eventPK", unique = true, nullable = false)
    public int getEventPK() {
        return eventPK;
    }

    public void setEventPK(int eventPK) {
        this.eventPK = eventPK;
    }

    @Column(name = "eventId")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonIgnore
    @Column(name = "ecName")
    public String getEcName() {
        return ecName;
    }

    public void setEcName(String ecName) {
        this.ecName = ecName;
    }

    @JsonIgnore
    @Column(name = "prefix")
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Column(name = "operationName")
    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Column(name = "partnerId")
    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    @JsonIgnore
    @Column(name = "negated")
    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    @JsonIgnore
    @Column(name = "abducible")
    public Boolean getAbducible() {
        return abducible;
    }

    public void setAbducible(Boolean abducible) {
        this.abducible = abducible;
    }

    @JsonIgnore
    @Column(name = "recordable")
    public Boolean getRecordable() {
        return recordable;
    }

    public void setRecordable(Boolean recordable) {
        this.recordable = recordable;
    }

    @JsonIgnore
    @Column(name = "eventObject")
    public Blob getEventObject() {
        return eventObject;
    }

    public void setEventObject(Blob eventObject) {
        this.eventObject = eventObject;
    }

    @JsonIgnore
    @Column(name = "eventString")
    public String getEventString() {
        return eventString;
    }

    public void setEventString(String eventString) {
        this.eventString = eventString;
    }

    @Column(name = "timestamp")
    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public void setParameters(List<EventParameter> parameters) {
        this.parameters = parameters;
    }

    @Transient
    public List<EventParameter> getParameters() {
        return parameters;
    }
}
