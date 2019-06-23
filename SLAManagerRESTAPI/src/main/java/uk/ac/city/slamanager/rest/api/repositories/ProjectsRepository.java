package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.Project;
import uk.ac.city.slamanager.rest.api.entities.User;

import javax.persistence.OrderBy;
import java.util.List;
import java.util.Set;

public interface ProjectsRepository extends CrudRepository<Project, Integer>{
	Project findById(Integer id);
	@OrderBy("created desc")
	List<Project> findByUserOrderByCreatedDesc(User user);

	Set<Project> findByIdIn(Set<Integer> pids);
	Project findByName(String name);
}
