package org.soundforme.repositories;

import org.soundforme.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author NGorelov
 */
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
}
