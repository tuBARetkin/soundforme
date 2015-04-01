package org.soundforme.repositories;

import org.soundforme.model.Release;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author NGorelov
 */
public interface ReleaseRepository extends MongoRepository<Release, String> {
    Release findByDiscogsId(int discogsId);
}
