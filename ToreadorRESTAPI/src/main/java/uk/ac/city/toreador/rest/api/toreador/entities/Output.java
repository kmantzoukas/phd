package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "outputs", catalog = "toreador")
public class Output implements java.io.Serializable {

	private Integer id;
	private Asset asset;
	private Operation operation;
	private String name;
	private String type;

	public Output() {
	}

	public Output(Asset asset, Operation operation, String name, String type) {
		this.asset = asset;
		this.operation = operation;
		this.name = name;
		this.type = type;
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
	@JoinColumn(name = "assetId")
	public Asset getAsset() {
		return this.asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oid")
	public Operation getOperation() {
		return this.operation;
	}

	public void setOperation(Operation operations) {
		this.operation = operations;
	}

	@Column(name = "name", length = 45)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "type", length = 45)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
