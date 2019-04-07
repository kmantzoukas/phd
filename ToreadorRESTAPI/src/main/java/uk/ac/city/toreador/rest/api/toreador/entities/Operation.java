package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "operations", catalog = "toreador")
public class Operation implements java.io.Serializable {

	private Integer id;
	private Asset asset;
	private AtomicService atomicService;
	private String name;
	private String inputmessage;
	private String outputmessage;
	private Set<Input> inputs = new HashSet<Input>(0);
	private Set<Output> outputs = new HashSet<Output>(0);

	public Operation() {
	}

	public Operation(Asset asset, AtomicService atomicService, String name, String inputmessage,
			String outputmessage, Set<Input> inputs, Set<Output> outputs) {
		this.asset = asset;
		this.atomicService = atomicService;
		this.name = name;
		this.inputmessage = inputmessage;
		this.outputmessage = outputmessage;
		this.inputs = inputs;
		this.outputs = outputs;
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

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "operation")
	public Set<Input> getInputs() {
		return this.inputs;
	}

	public void setInputs(Set<Input> inputs) {
		this.inputs = inputs;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "operation")
	public Set<Output> getOutputs() {
		return this.outputs;
	}

	public void setOutputs(Set<Output> outputs) {
		this.outputs = outputs;
	}

}
