package uk.ac.city.slamanager.rest.api.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.persistence.*;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "projects", catalog = "slamanager")
public class Project implements java.io.Serializable {

	private Integer id;
	private User user;
	private String name;
	private byte[] model;
	private byte[] properties;
	private byte[] validationoutput;
	private Date created;
	private ProjectStatus status;
	private String propertyCategoryCatalog;
	private byte[] wsagreement;
	private byte[] monitoringoutput;
	private ProjectType type;
	private List<Asset> assets = new ArrayList<>(0);
	private List<CompositeService> compositeservices = new ArrayList<>(0);
	private List<Slo> slos = new ArrayList<>(0);
	private String eventChannel;


	public Project() {
	}

	public Project(User user, String name, Date created, ProjectStatus status, String propertyCategoryCatalog) {
		this.user = user;
		this.name = name;
		this.created = created;
		this.status = status;
		this.propertyCategoryCatalog = propertyCategoryCatalog;
	}

	public Project(User users, String name, byte[] model, byte[] properties, byte[] validationoutput, Date created,
			ProjectStatus status, String propertyCategoryCatalog, byte[] wsagreement, byte[] monitoringoutput,
			List<Asset> assetses, List<CompositeService> compositeserviceses) {
		this.user = users;
		this.name = name;
		this.model = model;
		this.properties = properties;
		this.validationoutput = validationoutput;
		this.created = created;
		this.status = status;
		this.propertyCategoryCatalog = propertyCategoryCatalog;
		this.wsagreement = wsagreement;
		this.monitoringoutput = monitoringoutput;
		this.assets = assetses;
		this.compositeservices = compositeserviceses;
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId", nullable = false)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "name", nullable = false, length = 45)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonInclude(Include.ALWAYS)
	@Column(name = "model")
	public byte[] getModel() {
		return this.model;
	}

	public void setModel(byte[] model) {
		this.model = model;
	}
	
	@JsonInclude(Include.ALWAYS)
	@Column(name = "properties")
	public byte[] getProperties() {
		return this.properties;
	}

	public void setProperties(byte[] properties) {
		this.properties = properties;
	}
	
	@JsonInclude(Include.ALWAYS)
	@Column(name = "validationoutput")
	public byte[] getValidationoutput() {
		return this.validationoutput;
	}

	public void setValidationoutput(byte[] validationoutput) {
		this.validationoutput = validationoutput;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created", nullable = false, length = 19)
	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 10)
	public ProjectStatus getStatus() {
		return this.status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	@Column(name = "propertycategorycatalog", nullable = false, length = 5)
	public String getPropertyCategoryCatalog() {
		return this.propertyCategoryCatalog;
	}

	public void setPropertyCategoryCatalog(String propertyCategoryCatalog) {
		this.propertyCategoryCatalog = propertyCategoryCatalog;
	}
	
	@JsonInclude(Include.ALWAYS)
	@Column(name = "wsagreement")
	public byte[] getWsagreement() {
		return this.wsagreement;
	}

	public void setWsagreement(byte[] wsagreement) {
		this.wsagreement = wsagreement;
	}
	
	@JsonIgnore
	@Column(name = "monitoringoutput")
	public byte[] getMonitoringoutput() {
		return this.monitoringoutput;
	}

	public void setMonitoringoutput(byte[] monitoringoutput) {
		this.monitoringoutput = monitoringoutput;
	}

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
	public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "project")
	public List<Asset> getAssets() {
		return this.assets;
	}

	public void setAssets(List<Asset> assets) {
		this.assets = assets;
	}
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "project")
	public List<CompositeService> getCompositeservices() {
		return this.compositeservices;
	}

	public void setCompositeservices(List<CompositeService> compositeservices) {
		this.compositeservices = compositeservices;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "project")
	@OrderBy("id")
	public List<Slo> getSlos() {
		return slos;
	}

	public void setSlos(List<Slo> slos) {
	    this.slos = slos;
	}

	@Column(name = "eventChannel")
	public String getEventChannel() {
		return eventChannel;
	}

	public void setEventChannel(String eventChannel) {
		this.eventChannel = eventChannel;
	}
}
