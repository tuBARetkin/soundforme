package org.soundforme.external

import org.soundforme.config.SharedConfig
import org.soundforme.model.Release
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.tuple
/**
 * @author NGorelov
 */
@ContextConfiguration(classes = SharedConfig.class)
class ReleaseCollectorSpecification extends Specification {
    @Inject
    def ReleaseCollector releaseCollector;
    @Inject
    def DiscogsStore discogsStore;

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
        subscription.discogsId = 1;

        when:
        releaseCollector.collectAll(new Subscription())

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("type of subscription")
    }

    def "collecting should not work without discogs id"() {
        setup:
        def subscription = new Subscription()
        subscription.type = SubscriptionType.ARTIST

        when:
        releaseCollector.collectAll(subscription)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("id from discogs")
    }

    def "collector should results from all pages with not empty artist, title, discogsId and tracklist"() {
        setup:
        def xlr8rLabel = 103757;
        def subscription = new Subscription([
                discogsId: xlr8rLabel,
                type: SubscriptionType.LABEL
        ])

        def pagination = discogsStore.getLabelReleasesPage(xlr8rLabel, 1).get().pagination
        def releasesCount = pagination.items

        when:
        Set<Release> releases = releaseCollector.collectAll(subscription)

        then:
        assertThat(releases).isNotNull()
                .isNotEmpty()
                .hasSize(releasesCount)
        assertThat(releases).extracting("artist", "title")
                .doesNotContainNull()
                .doesNotContain(tuple(""))
        releases.forEach({release ->
            assertThat(release.trackList).extracting("title")
                    .doesNotContain("")
                    .doesNotContainNull()
        })
    }

    def "collector should not return already collected items"() {
        setup:
        def litCityTraxLabel = 403665
        def subscription = new Subscription([
                discogsId: litCityTraxLabel,
                type: SubscriptionType.LABEL,
                collectedReleases: [3663394, 4862147, 5030196, 6135073, 6441330]
        ])

        def pagination = discogsStore.getLabelReleasesPage(litCityTraxLabel, 1).get().pagination
        def releasesCount = pagination.items

        when:
        Set<Release> releases = releaseCollector.collectAll(subscription);

        then:
        assertThat(releases).isNotNull()
                .isNotEmpty()
                .hasSize(releasesCount - subscription.collectedReleases.size())
    }

    def "collector should not work with illegal artist or release source"() {
        setup:
        def illegalArtistPage = 111111111
        def subscription = new Subscription([
                discogsId: illegalArtistPage,
                type: SubscriptionType.ARTIST
        ])

        when:
        releaseCollector.collectAll(subscription)

        then:
        def e = thrown(ReleaseCollectingException)
        assertThat(e).isInstanceOf(ReleaseCollectingException)
                .hasMessageContaining("Error on loading first page of subscription")
    }


}
