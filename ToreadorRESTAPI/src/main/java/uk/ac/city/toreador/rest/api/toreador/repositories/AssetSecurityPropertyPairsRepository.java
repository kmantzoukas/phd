package uk.ac.city.toreador.rest.api.toreador.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.AssetSecurityPropertyPair;
import uk.ac.city.toreador.rest.api.toreador.entities.AssetsSecuritypropertiesId;

public interface AssetSecurityPropertyPairsRepository extends CrudRepository<AssetSecurityPropertyPair, AssetsSecuritypropertiesId> {

}
