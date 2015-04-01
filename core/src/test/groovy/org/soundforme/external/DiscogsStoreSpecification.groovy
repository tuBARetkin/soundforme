package org.soundforme.external

import org.soundforme.config.SharedConfig
import org.soundforme.external.model.ReleaseExternal
import org.springframework.test.context.ActiveProfiles
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
@ActiveProfiles("test")
class DiscogsStoreSpecification extends Specification {
    private static final int KISS_BAND_ID = 153073;
    private static final int MERCURY_LABEL_ID = 39357;
    private static final int ALIVE_ALBUM_ID = 702835;
    private static final int CASABLANCA_LABEL_ID = 2152;

    @Inject
    def DiscogsStore discogsStore;

    def "if id < 1 IllegalArgumentException should be thrown"() {
        when: "calling method with illegal digit identifier"
        getResourceMethod(discogsStore)

        then: "IllegalArgumentException expected"
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")

        where: "testing methods are"
        getResourceMethod << [
                {store -> store.getArtistReleasesPage(-1, 1).get()},
                {store -> store.getLabelReleasesPage(0, 1).get()},
                {store -> store.getReleaseResource(0).get()}
        ]
    }

    def "if page < 1 IllegalArgumentException should be thrown"() {
        when:
        discogsStore.getArtistReleasesPage(1, 0).get()
        then:
        def getArtistEx = thrown(IllegalArgumentException)
        assertThat(getArtistEx).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")

        when:
        discogsStore.getLabelReleasesPage(1, 0).get()
        then:
        def getLabelEx = thrown(IllegalArgumentException)
        assertThat(getLabelEx).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }


    def "paginated methods should return result from all pages"() {
        setup:
        def artistId = KISS_BAND_ID
        def labelId = MERCURY_LABEL_ID

        when: "getting pages with releases of 'Kiss' artist"
        def kissFirstPage = discogsStore.getArtistReleasesPage(artistId, 1).get()
        def kissSecondPage = discogsStore.getArtistReleasesPage(artistId, 2).get()
        def kissLastPage = discogsStore.getArtistReleasesPage(artistId, kissFirstPage.pagination.pages).get()
        then:
        assertThat(kissFirstPage.pagination.page).isEqualTo(1)
        assertThat(kissSecondPage.pagination.page).isEqualTo(2)
        assertThat(kissLastPage.pagination.page).isEqualTo(kissFirstPage.pagination.pages)

        when: "getting pages with releases of 'Mercury' label"
        def casablancaFirstPage = discogsStore.getLabelReleasesPage(labelId, 1).get()
        def casablancaSecondPage = discogsStore.getLabelReleasesPage(labelId, 2).get()
        def casablancaLastPage = discogsStore.getLabelReleasesPage(labelId, casablancaFirstPage.pagination.pages).get()
        then:
        assertThat(casablancaFirstPage.pagination.page).isEqualTo(1)
        assertThat(casablancaSecondPage.pagination.page).isEqualTo(2)
        assertThat(casablancaLastPage.pagination.page).isEqualTo(casablancaFirstPage.pagination.pages)

    }

    def "paginated methods should return not empty releases list"() {
        setup:
        def artistId = KISS_BAND_ID
        def labelId = MERCURY_LABEL_ID

        when: "getting first page of 'Kiss' artist"
        def artistFirstPage = discogsStore.getArtistReleasesPage(artistId, 1).get()
        then:
        assertThat(artistFirstPage.releases).isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSize(100)
                .extracting("id", "resourceUrl")
                .isNotEmpty()

        when: "getting first page of 'Mercury' label"
        def labelFirstPage = discogsStore.getLabelReleasesPage(labelId, 1).get()
        then:
        assertThat(labelFirstPage.releases).isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSize(100)
                .extracting("id", "resourceUrl")
                .isNotEmpty()
    }

    def "if id is illegal ExecutionException should be thrown"() {
        setup: "illegal identifier"
        def illegalId = 11111111

        when: "calling any method with illegal identifier"
        getResourceMethod(illegalId, discogsStore)

        then: "exception expected"
        def e = thrown(ExecutionException)
        assertDiscogsConnectionException(e)

        where: "testing methods are"
        getResourceMethod << [
                {int id, store -> store.getArtistReleasesPage(id, 1).get()},
                {int id, store -> store.getLabelReleasesPage(id, 1).get()},
                {int id, store -> store.getReleaseResource(id).get()}
        ]
    }

    def "if page does not exist ExecutionException excepted"() {
        setup:
        def pageNumber = 100

        when:
        discogsStore.getArtistReleasesPage(KISS_BAND_ID, pageNumber).get()
        then:
        def getArtistEx = thrown(ExecutionException)
        assertDiscogsConnectionException(getArtistEx)

        when:
        discogsStore.getLabelReleasesPage(KISS_BAND_ID, pageNumber).get()
        then:
        def getLabelEx = thrown(ExecutionException)
        assertDiscogsConnectionException(getLabelEx)
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

    def "getting name of artist or label should return not empty string"() {
        when:
        def bandName = discogsStore.getArtistNameById(KISS_BAND_ID)
        then:
        assertThat(bandName).isEqualTo("Kiss")

        when:
        def labelName = discogsStore.getLabelTitleById(MERCURY_LABEL_ID)
        then:
        assertThat(labelName).contains("Mercury")
    }

    def "if artist or label is illegal no exception expected"() {
        setup:
        def illegalResource = 111111111

        when:
        def bandName = discogsStore.getArtistNameById(illegalResource)
        then:
        noExceptionThrown()
        assertThat(bandName).isNull()

        when:
        def labelName = discogsStore.getLabelTitleById(illegalResource)
        then:
        noExceptionThrown()
        assertThat(labelName).isNull()
    }

    def assertDiscogsConnectionException(Throwable e){
        assertThat(e).isInstanceOf(ExecutionException)
                .hasCauseInstanceOf(DiscogsConnectionException)
                .hasMessageContaining("Error on connection to discogs")
    }
}
