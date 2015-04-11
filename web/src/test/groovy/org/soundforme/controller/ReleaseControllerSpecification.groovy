package org.soundforme.controller

import com.google.gson.Gson
import org.soundforme.config.SharedConfig
import org.soundforme.model.Release
import org.soundforme.repositories.ReleaseRepository
import org.soundforme.service.ReleaseService
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
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
    def ReleaseController releaseController
    @Inject
    def ReleaseRepository releaseRepository

    def MockMvc mockMvc
    def releaseService = Mock(ReleaseService)

    void setup() {
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
        def response = mockMvc.perform(put("/releases/{id}", id)
                .content(new Gson().toJson(new Release([
                discogsId: anotherDisogsId,
                starred: starred,
                checked: checked,
                releaseDate: anotherReleaseDate
            ])))
        )

        then:
        response.andExpect(status().isOk())
        def result = releaseRepository.findByDiscogsId(id)
        assertThat(result).isEqualToIgnoringGivenFields(releases.find {it.id == result.id}, "starred", "checked")
        assertThat(result).isNotEqualTo(releases.find {it.id == result.id})

        where:
        id      | anotherDisogsId   | starred   | checked   | anotherReleaseDate
        "100"   | 100               | true      | false     | "9999"
        "200"   | 999               | false     | true      | "2015"
        "300"   | 999               | true      | true      | "9999"
    }

    def "update should change both of starred and checked fields in one request"() {
        setup:
        releaseRepository.save(createRandomRelease(100))
        releaseRepository.save(createRandomRelease(200))
        releaseRepository.save(createRandomRelease(300))
        releaseRepository.save(createRandomRelease(400, true, true))

        when:
        def response = mockMvc.perform(put("/releases/{id}", id)
                .content(new Gson().toJson(new Release([
                        starred: starred,
                        checked: checked
                ])))
        )

        then:
        response.andExpect(status().isOk())
        def result = releaseRepository.findByDiscogsId(id)
        assertThat(result.starred).isEqualTo(starred)
        assertThat(result.checked).isEqualTo(checked)

        where:
        id      | starred   | checked
        "100"   | true      | false
        "200"   | false     | true
        "300"   | true      | true
        "400"   | false     | false
    }

    def "findAll should return all releases if pageable object is null"() {
        setup:
        100.times({
            releaseRepository.save(createRandomRelease(it))
        })

        when:
        def response = mockMvc.perform(get("/releases"))

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').value(hasSize(100)))
    }
}
