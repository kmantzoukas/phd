package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "parametervalues", catalog = "slamanager")
public class Parametervalues implements java.io.Serializable {

	private Integer id;
	private AssetSecurityPropertyPair assetsSecurityproperty;
	private Integer sloparameterId;
	private String value;

	public Parametervalues() {
	}

	public Parametervalues(AssetSecurityPropertyPair assetsSecurityproperty, Integer sloparameterId, String value) {
		this.assetsSecurityproperty = assetsSecurityproperty;
		this.sloparameterId = sloparameterId;
		this.value = value;
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
		  @JoinColumn(name = "aid", insertable = false, updatable = false),
		  @JoinColumn(name = "spid", insertable = false, updatable = false)
		})
	public AssetSecurityPropertyPair getAssetsSecurityproperty() {
		return this.assetsSecurityproperty;
	}

	public void setAssetsSecurityproperty(AssetSecurityPropertyPair assetsSecurityproperty) {
		this.assetsSecurityproperty = assetsSecurityproperty;
	}

	@Column(name = "sloparametersId")
	public Integer getSloparameterId() {
		return this.sloparameterId;
	}

	public void setSloparameterId(Integer sloparametersId) {
		this.sloparameterId = sloparametersId;
	}

	@Column(name = "value", length = 45)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
