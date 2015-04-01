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

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.tuple

/**
 * @author NGorelov
 */
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
            but should attach new subscription 
            and add release discogs id to collectedReleases of both subscriptions"""() {
        setup: "filling database with data"
        def subscriptionWithExistedRelease = new Subscription([
                title: "testArtist",
                discogsId: 100,
                type: SubscriptionType.ARTIST,
        ])
        def release = new Release([title: "existed", discogsId: 100])
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
        releaseCollector.collectAll(subscriptionWithSameRelease) >> [
                new Release([
                        title: "existed",
                        discogsId: 100
                ]
        )]


        when:
        subscriptionService.refresh()

        then:
        def savedRelease = releaseRepository.findOne(release.id)
        assertThat(savedRelease.subscriptions)
                .hasSize(2)
                .containsOnly(subscriptionWithExistedRelease, subscriptionWithSameRelease)
        assertThat(subscriptionWithExistedRelease.collectedReleases).containsOnly(100)
        assertThat(subscriptionWithSameRelease.collectedReleases).containsOnly(100)
    }

    def "refresh should add releases from collector to collectedReleases field"() {
        setup:
        def subscription = new Subscription([
                title: "testLabel",
                discogsId: 100,
                type: SubscriptionType.LABEL,
        ])
        subscription = subscriptionRepository.save(subscription)
        releaseCollector.collectAll(subscription) >> [
                new Release([
                        title: "test",
                        discogsId: 1
                ]),
                new Release([
                        title: "test",
                        discogsId: 2
                ])
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
                type: SubscriptionType.LABEL,
        ]));
        def artist = subscriptionRepository.save(new Subscription([
                title: "testArtist",
                discogsId: 101,
                type: SubscriptionType.ARTIST,
        ]));
        
        and: "mock release collector"
        subscriptionService.releaseCollector = releaseCollector
        def collectedDate = LocalDateTime.now()
        releaseCollector.collectAll(label) >> [
                new Release([
                        discogsId: 100,
                        artist: "testArtist1",
                        title: "album1",
                        releaseDate: "2015",
                        collectedDate: collectedDate,
                        label: "olo",
                        catNo: "olo01",
                        checked: false,
                        starred: false,
                        trackList: [
                                new Track([title: "track1", position: "A1", duration: "5:10"]),
                                new Track([title: "track2"])
                        ]
                ])
        ]
        releaseCollector.collectAll(artist) >> [
                new Release([
                        discogsId: 200,
                        artist: "testArtist2",
                        title: "album2",
                        releaseDate: "2015",
                        collectedDate: collectedDate,
                        label: "testLabel",
                        catNo: "test001X",
                        checked: false,
                        starred: false,
                        trackList: [
                                new Track([title: "track1", position: "A1", duration: "5:10"]),
                                new Track([title: "track2"])
                        ]
                ]),
                new Release([
                        discogsId: 300,
                        artist: "testArtist3",
                        title: "album3",
                        releaseDate: "2015",
                        collectedDate: collectedDate,
                        label: "testLabel",
                        catNo: "test002X",
                        checked: false,
                        starred: false,
                        trackList: [
                                new Track([title: "track1", position: "A1", duration: "5:10"]),
                                new Track([title: "track2"])
                        ]
                ])
        ]

        when:
        subscriptionService.refresh()

        then: "all releases saved"
        def savedReleases = releaseRepository.findAll();
        assertThat(savedReleases).hasSize(3)
                .extracting("discogsId", "artist", "title", "releaseDate", "collectedDate", "testLabel", "catNo", "checked", "starred")
                .containsOnly(tuple(100, 200, 300),
                        tuple("testArtist1", "testArtist2", "testArtist3"),
                        tuple("album1", "album2", "album3"),
                        tuple("2015"),
                        tuple(collectedDate),
                        tuple("olo", "testLabel"),
                        tuple("olo01", "test001X", "test002X"),
                        tuple(false),
                        tuple(false)
        );
        savedReleases.each {
                assertThat(it.subscriptions).hasSize(1)
                assertThat(it.trackList).hasSize(2)
                        .extracting("title").isNotEmpty()
        }
    }
    
    void cleanup() {
        subscriptionRepository.deleteAll()
    }
}
