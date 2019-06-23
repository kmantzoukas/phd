package uk.ac.city.slamanager.rest.api.everest.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.everest.entities.Event;

public interface EventRepository extends CrudRepository<Event, Integer> {

    Event findByEventId(String eventId);

}
