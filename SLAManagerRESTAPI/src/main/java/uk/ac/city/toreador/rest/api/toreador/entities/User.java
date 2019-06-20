package uk.ac.city.toreador.rest.api.toreador.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "users", catalog = "slamanager")
public class User implements java.io.Serializable {

	private Integer id;
	private String username;
	private String password;
	private Set<Project> projects = new HashSet<Project>(0);

	public User() {
	}

	public User(String name, String surname, String username, String password) {
		this.username = username;
		this.password = password;
	}

	public User(String name, String surname, String username, String password, Set<Project> projects) {
		this.username = username;
		this.password = password;
		this.projects = projects;
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

	@Column(name = "username", nullable = false, length = 45)
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonIgnore
	@Column(name = "password", nullable = false, length = 45)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(Set<Project> projects) {
		this.projects = projects;
	}

}
