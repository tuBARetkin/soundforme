package org.soundforme.service

import org.soundforme.config.SharedConfig
import org.soundforme.external.DiscogsConnectionException
import org.soundforme.external.ReleaseCollector
import org.soundforme.model.Release
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.soundforme.model.Track
import org.soundforme.repositories.ReleaseRepository
import org.soundforme.repositories.SubscriptionRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDateTime

import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

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

    def """refresh should not save already existed in DB releases 
            but should add existed release ref to both subscriptions"""() {
        setup: "filling database with data"
        def subscriptionWithExistedRelease = new Subscription([
                title: "testArtist",
                discogsId: 100,
                type: SubscriptionType.ARTIST,
                collectedReleases: [100]
        ])
        def release = createRandomRelease(100)
        def subscriptionWithSameRelease = new Subscription([
                title: "testLabel",
                discogsId: 101,
                type: SubscriptionType.LABEL
        ])
        release = releaseRepository.save(release)
        subscriptionWithExistedRelease.releases = [release]
        subscriptionWithExistedRelease = subscriptionRepository.save(subscriptionWithExistedRelease)
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
        def subscription = new Subscription([
                title: "testLabel",
                discogsId: 100,
                type: SubscriptionType.LABEL
        ])
        subscription = subscriptionRepository.save(subscription)

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
        def label = subscriptionRepository.save(new Subscription([
                title: "testLabel",
                discogsId: 100,
                type: SubscriptionType.LABEL
        ]));
        def artist = subscriptionRepository.save(new Subscription([
                title: "testArtist",
                discogsId: 101,
                type: SubscriptionType.ARTIST
        ]));
        
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

    def "if unsubscribe with null subscription NullPointerException expected"() {
        when:
        subscriptionService.unsubscribe(null)
        then:
        def e = thrown(NullPointerException)
        assertThat(e).isInstanceOf(NullPointerException).hasMessageContaining("should be defined")
    }

    def "if unsubscribe with null or empty id IllegalArgumentException expected"() {
        when:
        subscriptionService.unsubscribe(new Subscription([id: id]))

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("should not be empty")

        where:
        id << [null, ""]
    }

    def "unsubscribe should only change value of closed flag"() {
        setup:
        def label = subscriptionRepository.save(new Subscription([
                title: "testLabel",
                discogsId: 100,
                type: SubscriptionType.LABEL,
                closed: false
        ]))
        def artist = subscriptionRepository.save(new Subscription([
                title: "testArtist",
                discogsId: 200,
                type: SubscriptionType.ARTIST
        ]))

        when:
        subscriptionService.unsubscribe(label)
        then:
        assertThat(subscriptionRepository.findOne(label.getId()).closed).isTrue()

        when:
        subscriptionService.unsubscribe(artist)
        then:
        assertThat(subscriptionRepository.findOne(artist.getId()).closed).isTrue()
    }

    def createRandomRelease(id){
        new Release([
                discogsId: id,
                artist: randomUUID(),
                title: randomUUID(),
                releaseDate: "2015",
                collectedDate: LocalDateTime.now(),
                label: randomUUID(),
                catNo: randomUUID(),
                checked: false,
                starred: false,
                trackList: [
                        new Track([title: randomUUID(), position: "A1", duration: "5:10"]),
                        new Track([title: randomUUID()])
                ]
        ])
    }

    void cleanup() {
        subscriptionRepository.deleteAll()
        releaseRepository.deleteAll()
    }
}
