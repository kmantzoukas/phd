package uk.ac.city.slamanager.rest.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "slos", catalog = "slamanager")
public class Slo {

    private int id;
    private Project project;
    private Asset asset;
    private SecurityProperty property;
    private String ecassertion;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid")
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aid")
    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sid")
    public SecurityProperty getProperty() {
        return property;
    }

    public void setProperty(SecurityProperty property) {
        this.property = property;
    }

    @Column(name = "ecassertion")
    public String getEcassertion() {
        return ecassertion;
    }

    public void setEcassertion(String ecassertion) {
        this.ecassertion = ecassertion;
    }
}
