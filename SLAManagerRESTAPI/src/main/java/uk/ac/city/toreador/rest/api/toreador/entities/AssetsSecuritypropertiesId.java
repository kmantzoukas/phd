package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AssetsSecuritypropertiesId implements java.io.Serializable {

	private int aid;
	private int spid;

	public AssetsSecuritypropertiesId() {
	}

	public AssetsSecuritypropertiesId(int aid, int spid) {
		this.aid = aid;
		this.spid = spid;
	}

	@Column(name = "aid", nullable = false)
	public int getAid() {
		return this.aid;
	}
	
	@JsonProperty
	public void setAid(int aid) {
		this.aid = aid;
	}

	@Column(name = "spid", nullable = false)
	public int getSpid() {
		return this.spid;
	}
	
	@JsonProperty
	public void setSpid(int spid) {
		this.spid = spid;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof AssetsSecuritypropertiesId))
			return false;
		AssetsSecuritypropertiesId castOther = (AssetsSecuritypropertiesId) other;

		return (this.getAid() == castOther.getAid()) && (this.getSpid() == castOther.getSpid());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getAid();
		result = 37 * result + this.getSpid();
		return result;
	}

}
