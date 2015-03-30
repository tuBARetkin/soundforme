package org.soundforme.service
import org.soundforme.config.SharedConfig
import org.soundforme.external.DiscogsConnectionException
import org.soundforme.model.SubscriptionType
import org.soundforme.repositories.SubscriptionRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.shouldHaveThrown

/**
 * @author NGorelov
 */
@ContextConfiguration(classes = SharedConfig.class)
@ActiveProfiles("test")
class SubscriptionServiceSpecification extends Specification {

    @Inject
    def SubscriptionRepository subscriptionRepository
    @Inject
    def SubscriptionService subscriptionService

    def "new subscription should contain automatic loaded title"() {
        expect:
        subscriptionService.follow(id).title == titles

        where:
        id << ["a153073", "l39357"]
        titles << ["Kiss", "Mercury"]
    }

    def "if resource string id is null, then NullPointerException expected"() {
        when:
        subscriptionService.follow(null)

        then:
        def e = thrown(NullPointerException)
        assertThat(e).isInstanceOf(NullPointerException).hasMessageContaining("Id of discogs resource should be defined")
    }

    def "if string identifier does not match regexp, then IllegalArgumentException expected"() {
        when:
        subscriptionService.follow(id)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException)
                .hasMessageContaining("Id should match '[al][0-9]+'")

        where:
        id << ["a", "l", "000", "al11", ""]
    }

    def "prefix of string identifier should have effect on new subscription type"() {
        expect:
        subscriptionService.follow(id).type == savedType

        where:
        id << ["a153073", "l39357"]
        savedType << [SubscriptionType.ARTIST, SubscriptionType.LABEL]
    }

    def "follow method should not work with illegal id"() {
        setup:
        def illegalId = "a111111111"

        when:
        subscriptionService.follow(illegalId)

        then:
        def e = thrown(DiscogsConnectionException)
        assertThat(e).isInstanceOf(DiscogsConnectionException).hasMessageContaining("not found")
    }

    def "follow method should save data to database"() {
        setup:
        def kissBandStringId = "a153073"

        when:
        subscriptionService.follow(kissBandStringId)

        then:
        def result = subscriptionRepository.findAll().get(0)
        assertThat(result.id).isNotEmpty()
        assertThat(result.discogsId).isEqualTo(kissBandStringId.substring(1) as int)
        assertThat(result.closed).isFalse()
        assertThat(result.type).isEqualTo(SubscriptionType.ARTIST)
        assertThat(result.title).isEqualTo("Kiss")
    }

    void cleanup() {
        subscriptionRepository.deleteAll()
    }
}
