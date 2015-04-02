package org.soundforme.service

import org.soundforme.config.SharedConfig
import org.soundforme.model.Release
import org.soundforme.repositories.ReleaseRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.soundforme.service.EntityObjectsBuilder.createRandomRelease

/**
 * @author NGorelov
 */
@SuppressWarnings("GroovyAccessibility")

@ContextConfiguration(classes = SharedConfig.class)
@ActiveProfiles("test")
class ReleaseServiceSpecification extends Specification{
    @Inject
    private ReleaseService releaseService;
    @Inject
    private ReleaseRepository releaseRepository;

    def "all releaseService methods should not work with null objects"() {
        when:
        testMethod(releaseService)

        then:
        def e = thrown(NullPointerException)
        assertThat(e).isInstanceOf(NullPointerException).hasMessageContaining("should be defined")

        where:
        testMethod << [
                {releaseService -> releaseService.findOne(null)},
                {releaseService -> releaseService.markChecked(null, true)},
                {releaseService -> releaseService.markStarred(null, true)},
                {releaseService -> releaseService.loadPage(null)},
                {releaseService -> releaseService.loadStarredPage(null)}
        ]
    }

    def "findOne method should work only with not empty strings"() {
        when:
        releaseService.findOne("")
        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("should not be blank")
    }

    def "findOne should return existed release"() {
        setup: "filling db with release object"
        def release = releaseRepository.save(createRandomRelease(100))

        when:
        def result = releaseService.findOne(release.id)

        then:
        assertThat(result).isEqualToComparingFieldByField(release)
    }

    def "findOne should return null if release not found"() {
        when:
        def result = releaseService.findOne("wrong id")

        then:
        noExceptionThrown()
        assertThat(result).isNull()
    }

    def "starring or checking release methods should work with objects with not blank id"() {
        when:
        testMethod(releaseService)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("should not be blank")

        where:
        testMethod << [
                {releaseService -> releaseService.markChecked(new Release(), true)},
                {releaseService -> releaseService.markStarred(new Release(), true)},
        ]
    }

    def "markStarred or markChecked should set true to existed release and do nothing if release not found in db"() {
        setup: "filling db with release object"
        def release = releaseRepository.save(createRandomRelease(100))

        when: "starred existing release"
        releaseService.markStarred(new Release([id: release.id]), true)
        then:
        assertThat(releaseRepository.findOne(release.id).starred).isTrue()

        when: "checked existing release"
        releaseService.markChecked(new Release([id: release.id]), true)
        then:
        assertThat(releaseRepository.findOne(release.id).checked).isTrue()

        when: "starred release not found"
        releaseService.markStarred(new Release([id: "wrong_id"]), true)
        then:
        assertThat(releaseRepository.count()).isEqualTo(1)

        when: "checked release not found"
        releaseService.markChecked(new Release([id: "wrong_id"]), true)
        then:
        assertThat(releaseRepository.count()).isEqualTo(1)
    }

    def "findPage should return page of expected size"() {
        setup:
        def releases = []
        100.times {
            releases << releaseRepository.save(createRandomRelease(it + 1))
        }

        when:
        def firstPage = releaseService.loadPage(new PageRequest(0, 50, Sort.Direction.DESC, "collectedDate"))

        then:
        assertThat(firstPage.totalElements).isEqualTo(100)
        assertThat(firstPage.totalPages).isEqualTo(2)
        assertThat(firstPage.content).hasSize(50)
        firstPage.content.eachWithIndex { item, i ->
            assertThat(item).isEqualToComparingFieldByField(releases[releases.size - i - 1] as Release)
        }
    }

    @Ignore
    def "findStarred page should return page of expected size and only with starred=true"() {
        setup:
        def releases = []
        100.times {
            releases << releaseRepository.save(createRandomRelease(it + 1))
        }
    }

    void cleanup() {
        releaseRepository.deleteAll()
    }
}
