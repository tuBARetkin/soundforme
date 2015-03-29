package org.soundforme.external

import groovy.util.logging.Slf4j
import org.soundforme.config.SharedConfig
import org.soundforme.model.Release
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author NGorelov
 */
@ContextConfiguration(classes = SharedConfig.class)
@Slf4j
class ReleaseCollectorSpecification extends Specification {
    @Inject
    private ReleaseCollector releaseCollector;

    def "collecting should not work with nullable subscription"() {
        when:
            releaseCollector.collectAll(null)
        then:
            def e = thrown(NullPointerException)
            assertThat(e).isInstanceOf(NullPointerException).hasMessageContaining("subscription should be defined")
    }

    def "collecting should not work with subscription of null type"() {
        setup:
            def subscription = new Subscription()
            subscription.setDiscogsId(1);
        when:
            releaseCollector.collectAll(new Subscription())
        then:
            def e = thrown(IllegalArgumentException)
            assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("type of subscription")
    }

    def "collecting should not work without discogs id"() {
        setup:
            def subscription = new Subscription()
            subscription.setType(SubscriptionType.ARTIST)
        when:
            releaseCollector.collectAll(subscription)
        then:
            def e = thrown(IllegalArgumentException)
            assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("id from discogs")
    }

    def "test"() {
        setup:
        Subscription subscription = new Subscription()
        subscription.setDiscogsId(25386)
        subscription.setType(SubscriptionType.LABEL)
        when:
        Set<Release> releases = releaseCollector.collectAll(subscription);
        then:
        releases.isEmpty()
    }
}
