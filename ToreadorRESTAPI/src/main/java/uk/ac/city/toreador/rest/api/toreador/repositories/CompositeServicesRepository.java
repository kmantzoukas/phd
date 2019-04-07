package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.CompositeService;

public interface CompositeServicesRepository extends CrudRepository<CompositeService, Integer>{
	
}
