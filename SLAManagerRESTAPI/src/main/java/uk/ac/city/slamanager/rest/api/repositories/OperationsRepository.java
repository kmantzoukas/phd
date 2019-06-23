package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.Operation;

public interface OperationsRepository extends CrudRepository<Operation, Long> {

}
