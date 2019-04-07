package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.User;

public interface UsersRepository extends CrudRepository<User, Integer>{
	User findById(Integer id);
	User findByUsername(String username);
}
