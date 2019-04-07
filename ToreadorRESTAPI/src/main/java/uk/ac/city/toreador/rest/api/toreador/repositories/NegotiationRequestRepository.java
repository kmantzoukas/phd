package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.NegotiationRequest;

public interface NegotiationRequestRepository extends CrudRepository<NegotiationRequest, Integer>{
	
}
