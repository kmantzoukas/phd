package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "sloparameters", catalog = "toreador")
public class SloParameter implements java.io.Serializable {

	private int id;
	private Slotemplate sloTemplate;
	private String name;
	private ParameterType type;
	private String values;
	private List<String> possbileValues;

	public SloParameter() {
	}

	public SloParameter(int id) {
		this.id = id;
	}

	public SloParameter(int id, Slotemplate sloTemplate, String name) {
		this.id = id;
		this.sloTemplate = sloTemplate;
		this.name = name;
	}

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
	@JoinColumn(name = "slotid")
	public Slotemplate getSloTemplate() {
		return this.sloTemplate;
	}

	public void setSloTemplate(Slotemplate sloTemplate) {
	    this.sloTemplate = sloTemplate;
	}

	@Column(name = "name", length = 45)
	public String getName() {
	    return this.name;
	}

	public void setName(String name) {
	    this.name = name;
	}

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }
    @JsonIgnore
    @Column(name = "values")
    public void setValues(String values) {
        this.values = values;
    }

    public String getValues() {
        return values;
    }

    @Transient
    public List<String> getPossbileValues() {
	    return  (getValues() != null) ? Arrays.asList(getValues().split(",")) : Collections.emptyList();
    }
}