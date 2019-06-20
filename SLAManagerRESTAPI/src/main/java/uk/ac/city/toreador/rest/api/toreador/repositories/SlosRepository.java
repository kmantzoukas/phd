package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.Slo;

public interface SlosRepository extends CrudRepository<Slo, Integer> {
}
