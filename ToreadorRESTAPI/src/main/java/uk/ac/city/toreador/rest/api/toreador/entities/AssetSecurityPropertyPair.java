package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assetsecuritypropertypairs", catalog = "toreador")
public class AssetSecurityPropertyPair implements java.io.Serializable {

	private AssetsSecuritypropertiesId id;
	private Asset asset;
	private SecurityProperty securityProperty;
	private String rate;
	private String timeunit;
	private String assertion;
	private String type;
	private String platformMonitoringProperty;
	private String deploymentMonitoringProperty;
	private Set<Parametervalues> parametervalues = new HashSet<Parametervalues>(0);
	private Set<GuardedAction> guardedActions = new HashSet<GuardedAction>(0);
	private Set<GuaranteeTerm> guaranteeTerms = new HashSet<GuaranteeTerm>(0);

	public AssetSecurityPropertyPair() {
	}

	public AssetSecurityPropertyPair(AssetsSecuritypropertiesId id) {
		this.id = id;
	}

	public AssetSecurityPropertyPair(AssetsSecuritypropertiesId id,Asset asset, SecurityProperty securityProperty,
			String rate, String timeunit, String assertion, String type,String platformMonitoringProperty,
			String deploymentMonitoringProperty, 
			Set<Parametervalues> parametervalues,
			Set<GuardedAction> guardedActions,
			Set<GuaranteeTerm> guaranteeTerms) {
		this.id = id;
		this.asset = asset;
		this.securityProperty = securityProperty;
		this.rate = rate;
		this.timeunit = timeunit;
		this.assertion = assertion;
		this.type = type;
		this.platformMonitoringProperty = platformMonitoringProperty;
		this.deploymentMonitoringProperty = deploymentMonitoringProperty;
		this.parametervalues = parametervalues;
		this.guardedActions = guardedActions;
		this.guaranteeTerms = guaranteeTerms;
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "aid", column = @Column(name = "aid", nullable = false)),
			@AttributeOverride(name = "spid", column = @Column(name = "spid", nullable = false)) })
	public AssetsSecuritypropertiesId getId() {
		return this.id;
	}
	
	@JsonProperty
	public void setId(AssetsSecuritypropertiesId id) {
		this.id = id;
	}

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "aid", nullable = false, insertable = false, updatable = false)
	public Asset getAsset() {
		return this.asset;
	}

	public void setAsset(Asset assets) {
		this.asset = assets;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "spid", nullable = false, insertable = false, updatable = false)
	public SecurityProperty getSecurityProperty() {
		return this.securityProperty;
	}

	public void setSecurityProperty(SecurityProperty securityProperty) {
		this.securityProperty = securityProperty;
	}

	@Column(name = "rate", length = 45)
	public String getRate() {
		return this.rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	@Column(name = "timeunit", length = 45)
	public String getTimeunit() {
		return this.timeunit;
	}

	public void setTimeunit(String timeunit) {
		this.timeunit = timeunit;
	}

	@Column(name = "assertion", length = 65535)
	public String getAssertion() {
		return this.assertion;
	}

	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}
	
	@Column(name = "type", length = 45)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "platformMonitoringProperty", length = 45)
	public String getPlatformMonitoringProperty() {
		return this.platformMonitoringProperty;
	}

	public void setPlatformMonitoringProperty(String platformMonitoringProperty) {
		this.platformMonitoringProperty = platformMonitoringProperty;
	}

	@Column(name = "deploymentMonitoringProperty", length = 45)
	public String getDeploymentMonitoringProperty() {
		return this.deploymentMonitoringProperty;
	}

	public void setDeploymentMonitoringProperty(String deploymentMonitoringProperty) {
		this.deploymentMonitoringProperty = deploymentMonitoringProperty;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "assetsSecurityproperty")
	public Set<Parametervalues> getParametervalues() {
		return this.parametervalues;
	}

	public void setParametervalues(Set<Parametervalues> parametervalues) {
		this.parametervalues = parametervalues;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "assetSecurityPropertyPair")
	public Set<GuardedAction> getGuardedActions() {
		return this.guardedActions;
	}

	public void setGuardedActions(Set<GuardedAction> guardedactions) {
		this.guardedActions = guardedactions;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "assetSecurityPropertyPair")
	public Set<GuaranteeTerm> getGuaranteeTerms() {
		return this.guaranteeTerms;
	}

	public void setGuaranteeTerms(Set<GuaranteeTerm> guaranteeTerms) {
		this.guaranteeTerms = guaranteeTerms;
	}

//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "assetsSecuritypropertiesBySpid")
//	public Set<Parametervalues> getParametervaluesesForSpid() {
//		return this.parametervaluesesForSpid;
//	}
//
//	public void setParametervaluesesForSpid(Set<Parametervalues> parametervaluesesForSpid) {
//		this.parametervaluesesForSpid = parametervaluesesForSpid;
//	}
//
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "assetsSecuritypropertiesBySpid")
//	public Set<GuardedAction> getGuardedactionsesForSpid() {
//		return this.guardedactionsesForSpid;
//	}
//
//	public void setGuardedactionsesForSpid(Set<GuardedAction> guardedactionsesForSpid) {
//		this.guardedactionsesForSpid = guardedactionsesForSpid;
//	}

}
