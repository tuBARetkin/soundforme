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
    @Inject
    def SubscriptionService subscriptionService;

    def "subscription service should save new subscription with automatic loaded title"() {
        expect:
        subscriptionService.follow(id).title == titles

        where:
        id << ["a153073", "l39357"]
        titles << ["Kiss", "Mercury"]
    }

    void cleanup() {
        subscriptionRepository.deleteAll()
    }
}
