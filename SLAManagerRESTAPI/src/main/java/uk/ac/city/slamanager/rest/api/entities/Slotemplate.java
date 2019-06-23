package uk.ac.city.slamanager.rest.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "slotemplates", catalog = "slamanager")
public class Slotemplate implements java.io.Serializable {

	private int id;
	private SecurityProperty securityProperty;
	private String name;
	private String xml;
	private Set<SloParameter> sloParameters = new HashSet<SloParameter>(0);
	private String description;

	public Slotemplate() {
	}

	public Slotemplate(int id) {
		this.id = id;
	}

	public Slotemplate(int id, SecurityProperty securityProperty, String name, String xml,
			Set<SloParameter> sloParameters) {
		this.id = id;
		this.securityProperty = securityProperty;
		this.name = name;
		this.xml = xml;
		this.sloParameters = sloParameters;
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
	@JoinColumn(name = "spid")
	public SecurityProperty getSecurityProperty() {
		return this.securityProperty;
	}
	public void setSecurityProperty(SecurityProperty securityProperty) {
		this.securityProperty = securityProperty;
	}

	@Column(name = "name", length = 256)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "xml")
	public String getXml() {
		return this.xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "sloTemplate")
	public Set<SloParameter> getSloParameters() {
		return this.sloParameters;
	}
	public void setSloParameters(Set<SloParameter> sloParameters) {
		this.sloParameters = sloParameters;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
