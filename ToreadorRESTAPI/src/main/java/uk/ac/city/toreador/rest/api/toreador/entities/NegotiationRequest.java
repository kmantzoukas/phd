package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "negotiationrequests", catalog = "toreador")
public class NegotiationRequest implements java.io.Serializable {

	private Integer id;
	private Negotiation negotiation;
	private NegotiationRequestType type;
	private ModelType modelType;
	private Date created;
	private int serviceprice;
	private int servicestart;
	private int serviceend;
	private int servicestep;

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
	@JoinColumn(name = "nid")
	public Negotiation getNegotiation() {
		return negotiation;
	}

	public void setNegotiation(Negotiation negotiation) {
		this.negotiation = negotiation;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	public NegotiationRequestType getType() {
		return type;
	}

	public void setType(NegotiationRequestType type) {
		this.type = type;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "modeltype")
	public ModelType getModelType() {
		return this.modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created", nullable = false, length = 19)
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Column(name = "serviceprice", nullable = true, length = 11)
	public int getServiceprice() {
		return serviceprice;
	}

	public void setServiceprice(int serviceprice) {
		this.serviceprice = serviceprice;
	}

	@Column(name = "servicestart", nullable = true, length = 11)
	public int getServicestart() {
		return servicestart;
	}

	public void setServicestart(int servicestart) {
		this.servicestart = servicestart;
	}

	@Column(name = "serviceend", nullable = true, length = 11)
	public int getServiceend() {
		return serviceend;
	}

	public void setServiceend(int serviceend) {
		this.serviceend = serviceend;
	}

	@Column(name = "servicestep", nullable = true, length = 11)
	public int getServicestep() {
		return servicestep;
	}

	public void setServicestep(int servicestep) {
		this.servicestep = servicestep;
	}

}
