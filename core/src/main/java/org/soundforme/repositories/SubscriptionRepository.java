package org.soundforme.repositories;

import org.soundforme.model.Subscription;
import org.soundforme.model.SubscriptionType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author NGorelov
 */
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Subscription findByDiscogsIdAndType(int discogsId, SubscriptionType type);
    List<Subscription> findByClosed(boolean closed);
}
