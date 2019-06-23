package uk.ac.city.slamanager.rest.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "compositeservices", catalog = "slamanager")
public class CompositeService implements java.io.Serializable {

	private Integer id;
	private Project project;
	private String name;
	private byte[] owls;
	private List<AtomicService> atomicServices = new ArrayList<AtomicService>(0);

	public CompositeService() {
	}

	public CompositeService(Project project, String name, byte[] owls, List<AtomicService> atomicServices) {
		this.project = project;
		this.name = name;
		this.owls = owls;
		this.atomicServices = atomicServices;
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
	@JoinColumn(name = "pid")
	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Column(name = "name", length = 45)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	@Column(name = "owls")
	public byte[] getOwls() {
		return this.owls;
	}

	public void setOwls(byte[] owls) {
		this.owls = owls;
	}
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "compositeService")
	public List<AtomicService> getAtomicServices() {
		return this.atomicServices;
	}

	public void setAtomicServices(List<AtomicService> atomicServices) {
		this.atomicServices = atomicServices;
	}

}
