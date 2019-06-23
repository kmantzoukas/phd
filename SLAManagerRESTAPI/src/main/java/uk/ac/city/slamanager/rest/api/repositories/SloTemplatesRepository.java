package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.Slotemplate;

public interface SloTemplatesRepository extends CrudRepository<Slotemplate, Integer> {
}
