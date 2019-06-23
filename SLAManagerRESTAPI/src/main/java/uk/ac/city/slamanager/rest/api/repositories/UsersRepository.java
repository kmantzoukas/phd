package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.User;

public interface UsersRepository extends CrudRepository<User, Integer>{
	User findById(Integer id);
	User findByUsername(String username);
}
