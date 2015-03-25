package org.soundforme.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author NGorelov
 */
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
}
