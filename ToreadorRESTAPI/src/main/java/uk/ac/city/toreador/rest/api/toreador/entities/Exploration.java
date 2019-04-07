package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "explorations", catalog = "toreador")
public class Exploration {

    private int id;
    private NegotiationRequest negotiationRequest;
    private GuaranteeTerm guaranteeTerm;
    private Date created;
    private Integer failurerate;
    private Integer failureratestart;
    private Integer failurerateend;
    private Integer failureratestep;
    private Integer penalty;
    private Integer penaltystart;
    private Integer penaltyend;
    private Integer penaltystep;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nrid")
    public NegotiationRequest getNegotiationRequest() {
        return negotiationRequest;
    }

    @JsonProperty("negotiationRequest")
    public void setNegotiationRequest(NegotiationRequest negotiationRequest) {
        this.negotiationRequest = negotiationRequest;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gtid")
    public GuaranteeTerm getGuaranteeTerm() {
        return guaranteeTerm;
    }

    @JsonProperty("guaranteeTerm")
    public void setGuaranteeTerm(GuaranteeTerm guaranteeTerm) {
        this.guaranteeTerm = guaranteeTerm;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false, length = 19)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Column(name ="failurerate" , length = 11, nullable = true)
    public Integer getFailurerate() {
        return failurerate;
    }

    public void setFailurerate(int failurerate) {
        this.failurerate = failurerate;
    }

    @Column(name ="failureratestart" , length = 11, nullable = true)
    public Integer getFailureratestart() {
        return failureratestart;
    }

    public void setFailureratestart(int failureratestart) {
        this.failureratestart = failureratestart;
    }

    @Column(name ="failurerateend" , length = 11, nullable = true)
    public Integer getFailurerateend() {
        return failurerateend;
    }

    public void setFailurerateend(int failurerateend) {
        this.failurerateend = failurerateend;
    }

    @Column(name ="failureratestep" , length = 11, nullable = true)
    public Integer getFailureratestep() {
        return failureratestep;
    }

    public void setFailureratestep(int failureratestep) {
        this.failureratestep = failureratestep;
    }

    @Column(name ="penalty" , length = 11, nullable = true)
    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    @Column(name ="penaltystart" , length = 11, nullable = true)
    public Integer getPenaltystart() {
        return penaltystart;
    }

    public void setPenaltystart(int penaltystart) {
        this.penaltystart = penaltystart;
    }

    @Column(name ="penaltyend" , length = 11, nullable = true)
    public Integer getPenaltyend() {
        return penaltyend;
    }

    public void setPenaltyend(int penaltyend) {
        this.penaltyend = penaltyend;
    }

    @Column(name ="penaltystep" , length = 11, nullable = true)
    public Integer getPenaltystep() {
        return penaltystep;
    }

    public void setPenaltystep(int penaltystep) {
        this.penaltystep = penaltystep;
    }
}
