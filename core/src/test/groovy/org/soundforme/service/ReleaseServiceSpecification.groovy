package org.soundforme.service

import org.soundforme.config.SharedConfig
import org.soundforme.model.Release
import org.soundforme.repositories.ReleaseRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
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

    def "starring or checking release methods should work only with not blank ids"() {
        when:
        testMethod(releaseService)

        then:
        def e = thrown(IllegalArgumentException)
        assertThat(e).isInstanceOf(IllegalArgumentException).hasMessageContaining("should not be blank")

        where:
        testMethod << [
                {releaseService -> releaseService.setChecked(null, true)},
                {releaseService -> releaseService.setChecked("", true)},
                {releaseService -> releaseService.setStarred(null, true)},
                {releaseService -> releaseService.setStarred("", true)},
        ]
    }

    def "markStarred or markChecked should set values to existed release and do nothing if release not found in db"() {
        setup: "filling db with release object"
        def release = releaseRepository.save(createRandomRelease(100))

        when: "starred existing release"
        releaseService.setStarred(release.id, true)
        then:
        assertThat(releaseRepository.findOne(release.id).starred).isTrue()

        when: "starred = false existing release"
        releaseService.setStarred(release.id, false)
        then:
        assertThat(releaseRepository.findOne(release.id).starred).isFalse()

        when: "checked existing release"
        releaseService.setChecked(release.id, true)
        then:
        assertThat(releaseRepository.findOne(release.id).checked).isTrue()

        when: "checked = false existing release"
        releaseService.setChecked(release.id, false)
        then:
        assertThat(releaseRepository.findOne(release.id).checked).isFalse()
    }

    def "markStarred and markChecked should not work with not existed releases. EntityNotFoundException expected"() {
        when: "starred release not found"
        releaseService.setStarred("wrong_id", true)
        then:
        def e = thrown(EntityNotFoundException)
        assertThat(e).isInstanceOf(EntityNotFoundException).hasMessage("Release with id wrong_id not found in db")

        when: "checked release not found"
        releaseService.setChecked("wrong_id", true)
        then:
        def e2 = thrown(EntityNotFoundException)
        assertThat(e2).isInstanceOf(EntityNotFoundException).hasMessage("Release with id wrong_id not found in db")
    }

    def "findPage should return page of expected size and data"() {
        setup:
        def releases = []
        100.times {
            releases << releaseRepository.save(createRandomRelease(it + 1))
            Thread.sleep(100)
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

    def "findPage should return all pages"() {
        setup:
        def releases = []
        75.times {
            releases << releaseRepository.save(createRandomRelease(it + 1))
        }

        when:
        def firstPage = releaseService.loadPage(new PageRequest(0, 50, Sort.Direction.DESC, "collectedDate"))
        def lastPage = releaseService.loadPage(new PageRequest(1, 50, Sort.Direction.DESC, "collectedDate"))

        then:
        assertThat(firstPage.totalElements).isEqualTo(75)
        assertThat(firstPage.totalPages).isEqualTo(2)
        assertThat(firstPage.content).hasSize(50)
        assertThat(lastPage.totalElements).isEqualTo(75)
        assertThat(lastPage.totalPages).isEqualTo(2)
        assertThat(lastPage.content).hasSize(25)
    }

    def "findStarredPage should return only pages with starred setted to true"() {
        setup: "filling db with 100 releases half of which marked starred"
        def releases = []
        100.times {
            def release = createRandomRelease(it + 1)
            if((it + 1) % 2 == 0){
                release.starred = true
            }
            releases << releaseRepository.save(release)
        }

        when:
        def firstPage = releaseService.loadStarredPage(new PageRequest(0, 50, Sort.Direction.DESC, "collectedDate"))

        then:
        assertThat(firstPage.totalElements).isEqualTo(50)
        assertThat(firstPage.totalPages).isEqualTo(1)
        assertThat(firstPage.content).hasSize(50)
    }

    def "findStarredPage should return page of expected size and data"() {
        setup:
        def releases = []
        100.times {
            def release = createRandomRelease(it + 1)
            release.starred = true
            releases << releaseRepository.save(release)
            Thread.sleep(100)
        }

        when:
        def firstPage = releaseService.loadStarredPage(new PageRequest(0, 50, Sort.Direction.DESC, "collectedDate"))

        then:
        assertThat(firstPage.totalElements).isEqualTo(100)
        assertThat(firstPage.totalPages).isEqualTo(2)
        assertThat(firstPage.content).hasSize(50)
        firstPage.content.eachWithIndex { item, i ->
            assertThat(item).isEqualToComparingFieldByField(releases[releases.size - i - 1] as Release)
        }
    }

    def "findStarredPage should return all pages"() {
        setup:
        def releases = []
        75.times {
            def release = createRandomRelease(it + 1)
            release.starred = true
            releases << releaseRepository.save(release)
        }

        when:
        def firstPage = releaseService.loadStarredPage(new PageRequest(0, 50, Sort.Direction.DESC, "collectedDate"))
        def lastPage = releaseService.loadStarredPage(new PageRequest(1, 50, Sort.Direction.DESC, "collectedDate"))

        then:
        assertThat(firstPage.totalElements).isEqualTo(75)
        assertThat(firstPage.totalPages).isEqualTo(2)
        assertThat(firstPage.content).hasSize(50)
        assertThat(lastPage.totalElements).isEqualTo(75)
        assertThat(lastPage.totalPages).isEqualTo(2)
        assertThat(lastPage.content).hasSize(25)
    }

    void cleanup() {
        releaseRepository.deleteAll()
    }
}
