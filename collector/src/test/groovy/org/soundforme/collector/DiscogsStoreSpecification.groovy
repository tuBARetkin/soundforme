package org.soundforme.collector

import org.soundforme.config.SharedConfig
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author NGorelov
 */
@ContextConfiguration(classes = SharedConfig.class)
class DiscogsStoreSpecification extends Specification {
    private static final int KISS_BAND_ID = 153073;
    private static final int MERCURY_LABEL_ID = 39357;
    private static final int ALIVE_ALBUM_ID = 702835;

    @Inject
    private DiscogsStore discogsStore;

    def "getting release should not work with id < 1"(){
        when:
        discogsStore.getReleaseResource(0);

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }

    def "getting artist releases should not work with id < 1"(){
        when:
        discogsStore.getArtistReleasesPage(0, 1);

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }


    def "getting label releases should not work with id < 1"(){
        when:
        discogsStore.getLabelReleasesPage(0, 1);

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Release id")
    }

    def "getting artist releases should not work with page < 1"(){
        when:
        discogsStore.getArtistReleasesPage(1, 0);

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }

    def "getting label releases should not work with page < 1"(){
        when:
        discogsStore.getLabelReleasesPage(1, 0);

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("Page number")
    }

    def "getting artist releases should return any existing page of artist releases"(){
        setup: "setting up 'Kiss' band id from discogs service"
        def artistId = KISS_BAND_ID

        when:
        def firstPage = discogsStore.getArtistReleasesPage(artistId, 1);
        def secondPage = discogsStore.getArtistReleasesPage(artistId, 2);
        def lastPage = discogsStore.getArtistReleasesPage(artistId, secondPage.pagination.pages);

        then:
        assertThat(firstPage.pagination.page).isEqualTo(1)
        assertThat(secondPage.pagination.page).isEqualTo(2)
        assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getting artist releases should return not empty releases list"(){
        setup: "setting up 'Kiss' band id from discogs service"
        def artistId = KISS_BAND_ID

        when:
        def firstPage = discogsStore.getArtistReleasesPage(artistId, 1);

        then:
        assertThat(firstPage.releases).isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSize(100)
                .extracting("id", "resourceUrl")
                .isNotEmpty()
    }

    def "getting label releases should return any existing page of artist releases"(){
        setup: "setting up 'Casablanca' label id from discogs service"
        def labelId = MERCURY_LABEL_ID

        when:
        def firstPage = discogsStore.getLabelReleasesPage(labelId, 1);
        def secondPage = discogsStore.getLabelReleasesPage(labelId, 2);
        def lastPage = discogsStore.getLabelReleasesPage(labelId, secondPage.pagination.pages);

        then:
        assertThat(firstPage.pagination.page).isEqualTo(1)
        assertThat(secondPage.pagination.page).isEqualTo(2)
        assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getting label releases should return not empty releases list"(){
        setup: "setting up 'Mercury' label id from discogs service"
        def labelId = MERCURY_LABEL_ID

        when:
        def firstPage = discogsStore.getLabelReleasesPage(labelId, 1);

        then:
        assertThat(firstPage.releases).isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSize(100)
                .extracting("id", "resourceUrl")
                .isNotEmpty()
    }
}
