package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.GuardedAction;

public interface GuardedActionsRepository extends CrudRepository<GuardedAction, Integer>{
	
}
