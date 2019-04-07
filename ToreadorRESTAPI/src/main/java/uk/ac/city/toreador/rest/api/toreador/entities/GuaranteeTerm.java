package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "guaranteeterms", catalog = "toreador")
public class GuaranteeTerm {

    private int id;
    private AssetSecurityPropertyPair assetSecurityPropertyPair;
    private Set<Exploration> explorations = new HashSet<Exploration>();
    private String name;
    private TimeUnit unit;
    private String definition;
    private String rate;

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
    @JoinColumns({
            @JoinColumn(name = "aid", insertable = true, updatable = false),
            @JoinColumn(name = "spid", insertable = true, updatable = false)
    })
    public AssetSecurityPropertyPair getAssetSecurityPropertyPair() {
        return assetSecurityPropertyPair;
    }

    public void setAssetSecurityPropertyPair(AssetSecurityPropertyPair assetSecurityPropertyPair) {
        this.assetSecurityPropertyPair = assetSecurityPropertyPair;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "guaranteeTerm")
    public Set<Exploration> getExplorations() {
        return explorations;
    }

    public void setExplorations(Set<Exploration> explorations) {
        this.explorations = explorations;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    @Column(name = "definition")
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Column(name = "rate")
    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
