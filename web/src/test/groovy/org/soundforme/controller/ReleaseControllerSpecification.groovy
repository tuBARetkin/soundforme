package org.soundforme.controller

import com.google.gson.Gson
import org.soundforme.config.SharedConfig
import org.soundforme.model.Release
import org.soundforme.repositories.ReleaseRepository
import org.soundforme.service.ReleaseService
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.soundforme.service.EntityObjectsBuilder.createRandomRelease
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author NGorelov
 */
@SuppressWarnings(value = ["GroovyAssignabilityCheck", "GroovyAccessibility"])

@ContextConfiguration(classes = SharedConfig.class)
@ActiveProfiles("test")
@WebIntegrationTest
class ReleaseControllerSpecification extends Specification {
    @Inject
    def WebApplicationContext webApplicationContext;

    @Inject
    def ReleaseRepository releaseRepository

    def MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    def "update should not change fields except starred and checked"() {
        setup:
        def releases = [
                releaseRepository.save(createRandomRelease(100)),
                releaseRepository.save(createRandomRelease(200)),
                releaseRepository.save(createRandomRelease(300))
        ]

        when:
        def response = mockMvc.perform(put("/releases/{id}", releases[number].id)
                .contentType("application/json;charset=UTF-8")
                .content(new Gson().toJson(new Release([
                discogsId: anotherDisogsId,
                starred: starred,
                checked: checked,
                releaseDate: anotherReleaseDate
            ])))
        )

        then:
        response.andExpect(status().isOk())
        def result = releaseRepository.findOne(releases[number].id)
        assertThat(result).isEqualToIgnoringGivenFields(releases.find {it.id == result.id}, "starred", "checked")
        assertThat(result).isNotEqualTo(releases.find {it.id == result.id})

        where:
        number  | anotherDisogsId   | starred   | checked   | anotherReleaseDate
        0       | 100               | true      | false     | "9999"
        1       | 999               | false     | true      | "2015"
        2       | 999               | true      | true      | "9999"
    }

    def "update should change both of starred and checked fields in one request"() {
        setup:
        def releases = [
                releaseRepository.save(createRandomRelease(100)),
                releaseRepository.save(createRandomRelease(200)),
                releaseRepository.save(createRandomRelease(300)),
                releaseRepository.save(createRandomRelease(400, true, true))
        ]

        when:
        def response = mockMvc.perform(put("/releases/{id}", releases[number].id)
                .contentType("application/json;charset=UTF-8")
                .content(new Gson().toJson(new Release([
                        starred: starred,
                        checked: checked
                ])))
        )

        then:
        response.andExpect(status().isOk())
        def result = releaseRepository.findOne(releases[number].id)
        assertThat(result.starred).isEqualTo(starred)
        assertThat(result.checked).isEqualTo(checked)

        where:
        number  | starred   | checked
        0       | true      | false
        1       | false     | true
        2       | true      | true
        3       | false     | false
    }

    def "findAll should return expected page of releases"() {
        setup:
        99.times({
            releaseRepository.save(createRandomRelease(it))
        })

        when:
        def response = mockMvc.perform(get("/releases")
                .param("page", page as String)
                .param("size", 50 as String)
        )

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$.content').isArray())
                .andExpect(jsonPath('$.content').value(hasSize(expectedSize)))

        where:
        page << [0, 1]
        expectedSize << [50 ,49]
    }

    void cleanup() {
        releaseRepository.deleteAll()
    }
}
