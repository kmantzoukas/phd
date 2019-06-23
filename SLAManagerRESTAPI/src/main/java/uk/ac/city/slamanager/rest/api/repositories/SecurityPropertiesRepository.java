package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.SecurityProperty;

import java.util.List;

public interface SecurityPropertiesRepository extends CrudRepository<SecurityProperty, Integer> {
    public List<SecurityProperty> findAllByOrderByIdAsc();

    public SecurityProperty findByName(String name);
}
