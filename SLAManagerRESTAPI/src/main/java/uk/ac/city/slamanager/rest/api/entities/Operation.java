package uk.ac.city.slamanager.rest.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "operations", catalog = "slamanager")
public class Operation implements java.io.Serializable {

	private Integer id;
	private Asset asset;
	private AtomicService atomicService;
	private String name;
	private String inputmessage;
	private String outputmessage;

	public Operation() {
	}

	public Operation(Asset asset, AtomicService atomicService, String name, String inputmessage,
			String outputmessage) {
		this.asset = asset;
		this.atomicService = atomicService;
		this.name = name;
		this.inputmessage = inputmessage;
		this.outputmessage = outputmessage;
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
	@JoinColumn(name = "aid")
	public AtomicService getAtomicService() {
		return this.atomicService;
	}

	public void setAtomicService(AtomicService atomicService) {
		this.atomicService = atomicService;
	}

	@Column(name = "name", length = 45)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "inputmessage", length = 45)
	public String getInputmessage() {
		return this.inputmessage;
	}

	public void setInputmessage(String inputmessage) {
		this.inputmessage = inputmessage;
	}

	@Column(name = "outputmessage", length = 45)
	public String getOutputmessage() {
		return this.outputmessage;
	}

	public void setOutputmessage(String outputmessage) {
		this.outputmessage = outputmessage;
	}

}
