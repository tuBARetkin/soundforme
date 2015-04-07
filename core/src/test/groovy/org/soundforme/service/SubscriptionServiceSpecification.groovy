package org.soundforme.service

import org.soundforme.config.SharedConfig
import org.soundforme.external.DiscogsConnectionException
import org.soundforme.external.ReleaseCollector
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.soundforme.repositories.ReleaseRepository
import org.soundforme.repositories.SubscriptionRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.soundforme.service.EntityObjectsBuilder.createRandomRelease
import static org.soundforme.service.EntityObjectsBuilder.createRandomSubscription

/**
 * @author NGorelov
 */
@SuppressWarnings("GroovyAccessibility")

@ContextConfiguration(classes = SharedConfig.class)
@ActiveProfiles("test")
class SubscriptionServiceSpecification extends Specification {

    @Inject
    def SubscriptionRepository subscriptionRepository
    @Inject
    def ReleaseRepository releaseRepository;
    @Inject
    def SubscriptionService subscriptionService

    def releaseCollector = Mock(ReleaseCollector);

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

    def "if follow already existed but closed subscription mark it closed=false only"() {
        setup:
        def subscription = subscriptionRepository.save(createRandomSubscription(true, 100, true))

        when:
        subscriptionService.follow("l100")

        then:
        def result = subscriptionRepository.findOne(subscription.id)
        assertThat(result.closed).isFalse()
    }

    def """refresh should not save already existed in DB releases 
            but should add existed release ref to both subscriptions"""() {
        setup: "filling database with subscription with one release collected"
        def subscriptionWithExistedRelease = createRandomSubscription(false, 100, null)
        subscriptionWithExistedRelease.collectedReleases = [100]
        def release = createRandomRelease(100)
        release = releaseRepository.save(release)
        subscriptionWithExistedRelease.releases = [release]
        subscriptionWithExistedRelease = subscriptionRepository.save(subscriptionWithExistedRelease)

        and: "filling database with new empty subscription"
        def subscriptionWithSameRelease = createRandomSubscription(true, 101, null)
        subscriptionWithSameRelease = subscriptionRepository.save(subscriptionWithSameRelease)

        and: "mock releaseCollector invocation result"
        subscriptionService.releaseCollector = releaseCollector
        releaseCollector.collectAll(subscriptionWithExistedRelease) >> []
        releaseCollector.collectAll(subscriptionWithSameRelease) >> [release]

        when:
        subscriptionService.refresh()

        then:
        def savedWithExistedRelease = subscriptionRepository.findOne(subscriptionWithExistedRelease.id)
        def savedWithSameRelease = subscriptionRepository.findOne(subscriptionWithSameRelease.id)
        assertThat(savedWithExistedRelease.collectedReleases).containsOnly(100)
        assertThat(savedWithSameRelease.collectedReleases).containsOnly(100)
        assertThat(savedWithExistedRelease.releases).containsOnly(release)
        assertThat(savedWithSameRelease.releases).containsOnly(release)
    }

    def "refresh should add releases from collector to collectedReleases field"() {
        setup:
        def subscription = subscriptionRepository.save(createRandomSubscription(true, 100, null))

        and: "mock result of releaseCollector"
        subscriptionService.releaseCollector = releaseCollector
        releaseCollector.collectAll(subscription) >> [
                createRandomRelease(1),
                createRandomRelease(2)
        ]

        when:
        subscriptionService.refresh()

        then:
        def updatedSubscription = subscriptionRepository.findOne(subscription.id)
        assertThat(updatedSubscription.collectedReleases)
                .hasSize(2)
                .containsOnly(1, 2)
    }

    def "refresh should save collected releases to DB"(){
        setup: "filling database with subscriptions"
        def label = subscriptionRepository.save(createRandomSubscription(true, 100, null))
        def artist = subscriptionRepository.save(createRandomSubscription(false, 101, null))
        
        and: "mock result of collecting label release"
        subscriptionService.releaseCollector = releaseCollector
        def releaseOfLabel = createRandomRelease(100)
        releaseCollector.collectAll(label) >> [releaseOfLabel]

        and: "mock result of collecting artist releases"
        def firstArtistRelease = createRandomRelease(200)
        def secondArtistRelease = createRandomRelease(300)
        releaseCollector.collectAll(artist) >> [firstArtistRelease, secondArtistRelease]

        when:
        subscriptionService.refresh()

        then: "all releases saved"
        def savedReleases = releaseRepository.findAll();
        assertThat(savedReleases).hasSize(3)
                .containsOnly(firstArtistRelease, secondArtistRelease, releaseOfLabel)
        savedReleases.each {
                assertThat(it.trackList).hasSize(2)
                        .extracting("title").isNotEmpty()
        }
    }

    def "unsubscribe should work only with not blank id"() {
        when:
        subscriptionService.unsubscribe(id)
        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Subscription id should not be empty or null")
        where:
        id << [null, ""]
    }

    def "unsubscribe should only change value of closed flag"() {
        setup:
        def label = subscriptionRepository.save(createRandomSubscription(true, 100, false))
        def artist = subscriptionRepository.save(createRandomSubscription(false, 200, null))

        when: "unsubscribe with not null 'closed' flag"
        subscriptionService.unsubscribe(label.id)
        then:
        assertThat(subscriptionRepository.findOne(label.getId()).closed).isTrue()

        when: "unsubscribe with null 'closed'"
        subscriptionService.unsubscribe(artist.id)
        then:
        assertThat(subscriptionRepository.findOne(artist.getId()).closed).isTrue()
    }

    def "unsubscribe should throw EntityNotFoundException if no subscription in db"() {
        when:
        subscriptionService.unsubscribe("test")

        then:
        def e = thrown(EntityNotFoundException)
        assertThat(e).isInstanceOf(EntityNotFoundException).hasMessageContaining("not found in db")
    }

    def "findAll should return not closed subscriptions"() {
        setup:
        subscriptionRepository.save(createRandomSubscription(true, 100, false))
        subscriptionRepository.save(createRandomSubscription(true, 200, true))
        subscriptionRepository.save(createRandomSubscription(false, 300, null))

        when:
        def result = subscriptionService.findAll()

        then:
        assertThat(result).hasSize(2)
                .extracting("discogsId")
                .containsOnly(100, 300)
    }

    void cleanup() {
        subscriptionRepository.deleteAll()
        releaseRepository.deleteAll()
    }
}
