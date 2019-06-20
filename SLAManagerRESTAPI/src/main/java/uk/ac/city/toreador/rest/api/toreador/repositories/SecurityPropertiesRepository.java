package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.SecurityProperty;

import java.util.List;

public interface SecurityPropertiesRepository extends CrudRepository<SecurityProperty, Integer> {
    public List<SecurityProperty> findAllByOrderByIdAsc();

    public SecurityProperty findByName(String name);
}
