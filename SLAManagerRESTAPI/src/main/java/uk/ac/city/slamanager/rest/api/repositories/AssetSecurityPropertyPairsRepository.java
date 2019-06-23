package uk.ac.city.slamanager.rest.api.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.ac.city.slamanager.rest.api.entities.AssetSecurityPropertyPair;
import uk.ac.city.slamanager.rest.api.entities.AssetsSecuritypropertiesId;

public interface AssetSecurityPropertyPairsRepository extends CrudRepository<AssetSecurityPropertyPair, AssetsSecuritypropertiesId> {

}
