package uk.ac.city.toreador.rest.api.everest.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.everest.entities.TemplateDefined;

import java.util.List;

public interface TemplateDefinedRepository extends CrudRepository<TemplateDefined, Integer> {

    List<TemplateDefined> findByTemplateIdContaining(String property);

}
