package org.soundforme.controller

import org.soundforme.config.SharedConfig
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.soundforme.service.EntityObjectsBuilder
import org.soundforme.service.ReleaseService
import org.soundforme.service.SubscriptionService
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcBuilder
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.inject.Inject

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        1 * subscriptionService.follow("a100") >> new Subscription([type: SubscriptionType.ARTIST, discogsId: 100])
        response.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.type', is("ARTIST")))
                .andExpect(jsonPath('$.discogsId', is(100)))
    }

    def "controller should return new subscription with 400 code if label or artist not found"() {
        when:
        def response = mockMvc.perform(post("/subscriptions")
                .param("discogsStringId", "a100")
        )

        then:
        1 * subscriptionService.follow("a100") >> null
        response.andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.message', is("Resource not found")))
    }

}
