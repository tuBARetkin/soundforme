package org.soundforme.external

import org.soundforme.external.model.ReleaseExternal
import org.soundforme.config.SharedConfig
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject
import java.util.concurrent.ExecutionException

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
    def DiscogsStore discogsStore;

    def "getting release should not work with id < 1"() {
        when:
        discogsStore.getReleaseResource(0).get()

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }

    def "getting artist releases should not work with id < 1"() {
        when:
        discogsStore.getArtistReleasesPage(0, 1).get()

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }


    def "getting label releases should not work with id < 1"() {
        when:
        discogsStore.getLabelReleasesPage(0, 1).get()

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }

    def "getting artist releases should not work with page < 1"() {
        when:
        discogsStore.getArtistReleasesPage(1, 0).get()

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }

    def "getting label releases should not work with page < 1"() {
        when:
        discogsStore.getLabelReleasesPage(1, 0).get()

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }

    def "getting artist releases should return any existing page of artist releases"() {
        setup: "setting up 'Kiss' band id from discogs service"
        def artistId = KISS_BAND_ID

        when:
        def firstPage = discogsStore.getArtistReleasesPage(artistId, 1).get()
        def secondPage = discogsStore.getArtistReleasesPage(artistId, 2).get()
        def lastPage = discogsStore.getArtistReleasesPage(artistId, secondPage.pagination.pages).get()

        then:
        assertThat(firstPage.pagination.page).isEqualTo(1)
        assertThat(secondPage.pagination.page).isEqualTo(2)
        assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getting artist releases should return not empty releases list"() {
        setup: "setting up 'Kiss' band id from discogs service"
        def artistId = KISS_BAND_ID

        when:
        def firstPage = discogsStore.getArtistReleasesPage(artistId, 1).get()

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
        discogsStore.getArtistReleasesPage(illegalId, 1).get()

        then:
        def e = thrown(ExecutionException)
        assertDiscogsConnectionException(e)
    }

    def "getting artist releases should not work with unbounded page"() {
        setup:
        def pageNumber = 100

        when:
        discogsStore.getArtistReleasesPage(KISS_BAND_ID, pageNumber).get()

        then:
        def e = thrown(ExecutionException)
        assertDiscogsConnectionException(e)
    }

    def "getting label releases should return any existing page of artist releases"() {
        setup: "setting up 'Casablanca' label id from discogs service"
        def labelId = MERCURY_LABEL_ID

        when:
        def firstPage = discogsStore.getLabelReleasesPage(labelId, 1).get()
        def secondPage = discogsStore.getLabelReleasesPage(labelId, 2).get()
        def lastPage = discogsStore.getLabelReleasesPage(labelId, secondPage.pagination.pages).get()

        then:
        assertThat(firstPage.pagination.page).isEqualTo(1)
        assertThat(secondPage.pagination.page).isEqualTo(2)
        assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getting label releases should return not empty releases list"() {
        setup: "setting up 'Mercury' label id from discogs service"
        def labelId = MERCURY_LABEL_ID

        when:
        def firstPage = discogsStore.getLabelReleasesPage(labelId, 1).get()

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
        discogsStore.getLabelReleasesPage(illegalId, 1).get()

        then:
        def e = thrown(ExecutionException)
        assertDiscogsConnectionException(e)
    }

    def "getting label releases should not work with unbounded page"() {
        setup:
        def pageNumber = 100

        when:
        discogsStore.getLabelReleasesPage(KISS_BAND_ID, pageNumber).get()

        then:
        def e = thrown(ExecutionException)
        assertDiscogsConnectionException(e)
    }

    def "getting release should return object with filled artist, label, title and tracklist fields"() {
        setup:
        def releaseId = ALIVE_ALBUM_ID

        when:
        ReleaseExternal release = discogsStore.getReleaseResource(releaseId).get()

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
        discogsStore.getReleaseResource(illegalId).get()

        then:
        def e = thrown(ExecutionException)
        assertDiscogsConnectionException(e)
    }

    def "getting artist name should return not empty string"() {
        when:
        def bandName = discogsStore.getArtistNameById(KISS_BAND_ID)

        then:
        assertThat(bandName).isEqualTo("Kiss")
    }

    def "getting artist name should not throw exceptions in case of invalid id"() {
        when:
        def bandName = discogsStore.getArtistNameById(111111111)

        then:
        noExceptionThrown()
        assertThat(bandName).isNull()
    }

    def "getting label name should return not empty string"() {
        when:
        def labelName = discogsStore.getLabelTitleById(MERCURY_LABEL_ID)

        then:
        assertThat(labelName).contains("Mercury")
    }

    def "getting label name should not throw exceptions in case of invalid id"() {
        when:
        def labelName = discogsStore.getArtistNameById(111111111)

        then:
        noExceptionThrown()
        assertThat(labelName).isNull()
    }

    def static assertDiscogsConnectionException(Throwable e){
        assertThat(e).isInstanceOf(ExecutionException)
                .hasCauseInstanceOf(DiscogsConnectionException)
                .hasMessageContaining("Error on connection to discogs")
    }
}
