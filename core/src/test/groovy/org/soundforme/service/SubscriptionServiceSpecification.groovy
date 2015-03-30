package org.soundforme.service
import org.soundforme.config.SharedConfig
import org.soundforme.repositories.SubscriptionRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject
/**
 * @author NGorelov
 */
@ContextConfiguration(classes = SharedConfig.class)
@ActiveProfiles("test")
class SubscriptionServiceSpecification extends Specification {

    @Inject
    def SubscriptionRepository subscriptionRepository;

    void cleanup() {
        subscriptionRepository.deleteAll()
    }
}
