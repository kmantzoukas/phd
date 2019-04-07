package uk.ac.city.toreador.rest.api.toreador.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "negotiations", catalog = "toreador")
public class Negotiation {

    private int id;
    private Project project;
    private User user;
    private Date created;
    private NegotiationAction action;
    private Set<NegotiationRequest> negotiationRequests = new HashSet<>(0);

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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pid")
    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uid")
    public User getUser() {
        return user;
    }

    @JsonProperty
    public void setUser(User user) {
        this.user = user;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false, length = 19)
    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    public NegotiationAction getAction() {
        return action;
    }

    public void setAction(NegotiationAction action) {
        this.action = action;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "negotiation")
    public Set<NegotiationRequest> getNegotiationRequests() {
        return negotiationRequests;
    }

    public void setNegotiationRequests(Set<NegotiationRequest> negotiationRequests) {
        this.negotiationRequests = negotiationRequests;
    }
}
