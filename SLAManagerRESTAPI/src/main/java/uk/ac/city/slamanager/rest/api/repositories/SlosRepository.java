package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.Slo;

public interface SlosRepository extends CrudRepository<Slo, Integer> {
}
