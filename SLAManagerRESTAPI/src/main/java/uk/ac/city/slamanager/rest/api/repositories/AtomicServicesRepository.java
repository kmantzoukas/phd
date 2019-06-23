package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.AtomicService;

public interface AtomicServicesRepository extends CrudRepository<AtomicService, Integer>{
	
}
