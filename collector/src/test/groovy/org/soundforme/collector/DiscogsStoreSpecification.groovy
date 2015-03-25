package org.soundforme.collector

import org.soundforme.collector.model.ArtistReleasesPage
import org.soundforme.collector.model.PaginatedResource
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
    private static final int ALIVE_ALBUM_ID = 702835;

    @Inject
    private DiscogsStore discogsStore;

    def "getResource(id, page, resultType) should not work with id < 1"(){
        when:
            discogsStore.getResource(0, 1, ArtistReleasesPage.class);
        then:
            def e = thrown(NullPointerException)
            assertThat(e).isInstanceOf(NullPointerException).hasMessageContaining("id should be defined")
    }

    def "getResource(id, page, resultType) should not work with nullable result type"(){
        when:
        discogsStore.getResource(1, 1, ArtistReleasesPage.class);
        then:
        def e = thrown(NullPointerException)
        assertThat(e).isInstanceOf(NullPointerException).hasMessageContaining("result type should be defined")
    }

    def "getResource(id, page, resultType) should not work with page < 1"() {
        when:
            discogsStore.getResource(1, 0, ArtistReleasesPage.class);
        then:
            def e = thrown(IllegalArgumentException)
            assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("page number")
    }

    def "getResource(id, page, resultType) should work with null page"() {
        when:
            discogsStore.getResource(1, null, ArtistReleasesPage.class);
        then:
            notThrown(NullPointerException)
    }

    def "getResource(id, page, resultType) should not work with type that does not present in apiPatterns"() {
        when:
            discogsStore.getResource(1, 1, new PaginatedResource(){} as Class);
        then:
            def e = thrown(IllegalArgumentException)
            assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("result type should be")
    }

    def "getResource(id, page, resultType) should return any existing page of artist releases"(){
        setup: "setting up 'Kiss' band id from discogs service"
            def artistId = KISS_BAND_ID
        when:
            def firstPage = discogsStore.getResource(artistId, 1, ArtistReleasesPage.class);
            def secondPage = discogsStore.getResource(artistId, 2, ArtistReleasesPage.class);
            def lastPage = discogsStore.getResource(artistId, secondPage.pagination.pages, ArtistReleasesPage.class);
        then:
            assertThat(firstPage.pagination.page).isEqualTo(1)
            assertThat(secondPage.pagination.page).isEqualTo(2)
            assertThat(lastPage.pagination.page).isEqualTo(firstPage.pagination.pages)
    }

    def "getResource(id, page, resultType) should return not empty releases list"(){
        setup: "setting up 'Kiss' band id from discogs service"
            def artistId = KISS_BAND_ID
        when:
            def firstPage = discogsStore.getResource(artistId, 1, ArtistReleasesPage.class);
        then:
            assertThat(firstPage.releases).isNotNull()
                    .isNotEmpty()
                    .doesNotContainNull()
                    .hasSize(50)
                    .extracting("id", "resourceUrl")
                    .isNotEmpty()
    }

    def "getResource(id, resultType) should return not pageable object"(){
        setup: "setting up 'Kiss ‎– Alive!' album id"
            def releaseId = ALIVE_ALBUM_ID;
        when:
            def releae = discogsStore.getResource(releaseId, )
    }
}
