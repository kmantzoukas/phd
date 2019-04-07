package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.Negotiation;

import java.util.Set;

public interface NegotiationsRepository extends CrudRepository<Negotiation, Integer> {

    @Query(value = "SELECT * FROM toreador.negotiations where uid=?1 and action <> 'REJECT'", nativeQuery = true)
    Set<Negotiation> findByUser(Integer uid);
}
