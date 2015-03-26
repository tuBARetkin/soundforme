package org.soundforme.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author NGorelov
 */
public interface ReleaseRepository extends MongoRepository<Release, String> {
    List<Release> findBySubscriptionsIn(Collection<Subscription> subscriptions);
}
