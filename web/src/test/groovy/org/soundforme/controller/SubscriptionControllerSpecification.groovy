package org.soundforme.controller

import org.soundforme.config.SharedConfig
import org.soundforme.external.DiscogsConnectionException
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.soundforme.service.SubscriptionService
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.inject.Inject

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * @author NGorelov
 */
@SuppressWarnings(value = ["GroovyAssignabilityCheck", "GroovyAccessibility"])

@ContextConfiguration(classes = SharedConfig.class)
@ActiveProfiles("test")
@WebIntegrationTest
class SubscriptionControllerSpecification extends Specification {
    @Inject
    def WebApplicationContext webApplicationContext;
    @Inject
    def SubscriptionController subscriptionController

    def MockMvc mockMvc
    def subscriptionService = Mock(SubscriptionService)

    void setup() {
        subscriptionController.subscriptionService = subscriptionService
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    def "controller should return new subscription with 201 code if label or artist exists in discogs or in db"() {
        when:
        def response = mockMvc.perform(post("/subscriptions")
                .param("discogsStringId", "a100")
        )

        then:
        1 * subscriptionService.follow("a100") >> new Subscription([
                type: SubscriptionType.ARTIST,
                discogsId: 100,
                title: "testArtist"
        ])
        response.andExpect(status().isCreated())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("type").value("ARTIST"))
                .andExpect(jsonPath("discogsId").value(100))
                .andExpect(jsonPath("title").value("testArtist"))
    }

    def "controller should return 400 code if label or artist not found"() {
        when:
        def response = mockMvc.perform(post("/subscriptions")
                .param("discogsStringId", "a100")
        )

        then:
        1 * subscriptionService.follow("a100") >> null
        response.andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("message").value("Subscription a100 not found"))
    }

    def "controller should return 400 code if connection to discogs problems"() {
        when:
        def response = mockMvc.perform(post("/subscriptions")
                .param("discogsStringId", "a100")
        )

        then:
        1 * subscriptionService.follow("a100") >> {throw new DiscogsConnectionException()}
        response.andExpect(status().isBadRequest())
                .andExpect(status().reason("Error on connection to discogs"))
    }
}
