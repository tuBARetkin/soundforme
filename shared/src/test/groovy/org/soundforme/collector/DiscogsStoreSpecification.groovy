package org.soundforme.collector

import org.soundforme.external.DiscogsConnectionException
import org.soundforme.external.DiscogsStore
import org.soundforme.external.model.ReleaseExternal
import org.soundforme.config.SharedConfig
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.tuple

/**
 * @author NGorelov
 */
@ContextConfiguration(classes = SharedConfig.class)
class DiscogsStoreSpecification extends Specification {
    private static final int KISS_BAND_ID = 153073;
    private static final int MERCURY_LABEL_ID = 39357;
    private static final int ALIVE_ALBUM_ID = 702835;
    private static final int CASABLANCA_LABEL_ID = 2152;

    @Inject
    private DiscogsStore discogsStore;

    def "getting release should not work with id < 1"() {
        when:
        discogsStore.getReleaseResource(0)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }

    def "getting artist releases should not work with id < 1"() {
        when:
        discogsStore.getArtistReleasesPage(0, 1)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }


    def "getting label releases should not work with id < 1"() {
        when:
        discogsStore.getLabelReleasesPage(0, 1)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }

    def "getting artist releases should not work with page < 1"() {
        when:
        discogsStore.getArtistReleasesPage(1, 0)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }

    def "getting label releases should not work with page < 1"() {
        when:
        discogsStore.getLabelReleasesPage(1, 0)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }

    def "getting artist releases should return any existing page of artist releases"() {
        setup: "setting up 'Kiss' band id from discogs service"
        def artistId = KISS_BAND_ID

        when:
        def firstPage = discogsStore.getArtistReleasesPage(artistId, 1)
        def secondPage = discogsStore.getArtistReleasesPage(artistId, 2)
        def lastPage = discogsStore.getArtistReleasesPage(artistId, secondPage.pagination.pages)

        then:
        assertThat(firstPage.pagination.page).isEqualTo(1)
        assertThat(secondPage.pagination.page).isEqualTo(2)
        assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getting artist releases should return not empty releases list"() {
        setup: "setting up 'Kiss' band id from discogs service"
        def artistId = KISS_BAND_ID

        when:
        def firstPage = discogsStore.getArtistReleasesPage(artistId, 1)

        then:
        assertThat(firstPage.releases).isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSize(100)
                .extracting("id", "resourceUrl")
                .isNotEmpty()
    }

    def "getting artist releases should not work with illegal resources"() {
        setup:
        def illegalId = 11111111

        when:
        discogsStore.getArtistReleasesPage(illegalId, 1)

        then:
        def e = thrown(DiscogsConnectionException)
        assertThat(e).isInstanceOf(DiscogsConnectionException).hasMessage("Error on connection to discogs")
    }

    def "getting artist releases should not work with unbounded page"() {
        setup:
        def pageNumber = 100

        when:
        discogsStore.getArtistReleasesPage(KISS_BAND_ID, pageNumber)

        then:
        def e = thrown(DiscogsConnectionException)
        assertThat(e).isInstanceOf(DiscogsConnectionException).hasMessage("Error on connection to discogs")
    }

    def "getting label releases should return any existing page of artist releases"() {
        setup: "setting up 'Casablanca' label id from discogs service"
        def labelId = MERCURY_LABEL_ID

        when:
        def firstPage = discogsStore.getLabelReleasesPage(labelId, 1)
        def secondPage = discogsStore.getLabelReleasesPage(labelId, 2)
        def lastPage = discogsStore.getLabelReleasesPage(labelId, secondPage.pagination.pages)

        then:
        assertThat(firstPage.pagination.page).isEqualTo(1)
        assertThat(secondPage.pagination.page).isEqualTo(2)
        assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getting label releases should return not empty releases list"() {
        setup: "setting up 'Mercury' label id from discogs service"
        def labelId = MERCURY_LABEL_ID

        when:
        def firstPage = discogsStore.getLabelReleasesPage(labelId, 1)

        then:
        assertThat(firstPage.releases).isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSize(100)
                .extracting("id", "resourceUrl")
                .isNotEmpty()
    }

    def "getting label releases should not work with illegal resources"() {
        setup:
        def illegalId = 11111111

        when:
        discogsStore.getLabelReleasesPage(illegalId, 1)

        then:
        def e = thrown(DiscogsConnectionException)
        assertThat(e).isInstanceOf(DiscogsConnectionException).hasMessage("Error on connection to discogs")
    }

    def "getting label releases should not work with unbounded page"() {
        setup:
        def pageNumber = 100

        when:
        discogsStore.getLabelReleasesPage(KISS_BAND_ID, pageNumber)

        then:
        def e = thrown(DiscogsConnectionException)
        assertThat(e).isInstanceOf(DiscogsConnectionException).hasMessage("Error on connection to discogs")
    }

    def "getting release should return object with filled artist, label, title and tracklist fields"() {
        setup:
        def releaseId = ALIVE_ALBUM_ID

        when:
        ReleaseExternal release = discogsStore.getReleaseResource(releaseId)

        then:
        assertThat(release).isNotNull()
        assertThat(release.artists).hasSize(1)
                .extracting("id", "name")
                .contains(tuple(KISS_BAND_ID, "Kiss"))
        assertThat(release.labels).hasSize(1)
                .extracting("id", "catNo", "name")
                .contains(tuple(CASABLANCA_LABEL_ID, "NBLP 7020", "Casablanca Records"))
        assertThat(release.title).isEqualTo("Alive!")
        assertThat(release.tracklist).hasSize(16)
                .extracting("duration", "position", "title")
                .doesNotContainNull()
    }

    def "getting release should not work with illegal resources"() {
        setup:
        def illegalId = 11111111

        when:
        discogsStore.getReleaseResource(illegalId)

        then:
        def e = thrown(DiscogsConnectionException)
        assertThat(e).isInstanceOf(DiscogsConnectionException).hasMessage("Error on connection to discogs")
    }
}
