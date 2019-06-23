package uk.ac.city.slamanager.rest.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "assets", catalog = "slamanager")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Asset implements java.io.Serializable {

	private Integer id;
	private Project project;
	private String name;
	private String type;
	private Operation operation;
	private Set<AssetSecurityPropertyPair> assetSecuritypropertyPairs = new HashSet<AssetSecurityPropertyPair>(0);

	public Asset() {
	}

	public Asset(Project project, String name, String type, Operation operation, Set<AssetSecurityPropertyPair> assetSecuritypropertyPairs) {
		this.project = project;
		this.name = name;
		this.type = type;
		this.operation = operation;
		this.assetSecuritypropertyPairs = assetSecuritypropertyPairs;
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
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "pid")
	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Column(name = "name", length = 512)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "type", length = 10)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@OneToOne(fetch = FetchType.EAGER, mappedBy = "asset")
	public Operation getOperation() {
		return this.operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "asset")
	public Set<AssetSecurityPropertyPair> getAssetSecuritypropertyPairs() {
		return this.assetSecuritypropertyPairs;
	}

	public void setAssetSecuritypropertyPairs(Set<AssetSecurityPropertyPair> assetSecuritypropertyPairs) {
		this.assetSecuritypropertyPairs = assetSecuritypropertyPairs;
	}

}
