package uk.ac.city.slamanager.rest.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "securityproperties", catalog = "slamanager")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SecurityProperty implements java.io.Serializable {

	private Integer id;
	private String name;
	private List<Slotemplate> slotemplates = new ArrayList<Slotemplate>(10);
	private Set<AssetSecurityPropertyPair> assetSecuritypropertyPairs = new HashSet<AssetSecurityPropertyPair>(0);

	public SecurityProperty() {
	}

	public SecurityProperty(String name, List<Slotemplate> slotemplates) {
		this.name = name;
		this.slotemplates = slotemplates;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "name", length = 256)
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "securityProperty")
	public List<Slotemplate> getSlotemplates() {
		return this.slotemplates;
	}

	public void setSlotemplates(List<Slotemplate> slotemplates) {
		this.slotemplates = slotemplates;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "securityProperty")
	public Set<AssetSecurityPropertyPair> getAssetSecuritypropertyPairs() {
		return this.assetSecuritypropertyPairs;
	}

	public void setAssetSecuritypropertyPairs(Set<AssetSecurityPropertyPair> assetSecuritypropertyPairs) {
		this.assetSecuritypropertyPairs = assetSecuritypropertyPairs;
	}

}
