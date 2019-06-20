package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.Slotemplate;

public interface SloTemplatesRepository extends CrudRepository<Slotemplate, Integer> {
}
