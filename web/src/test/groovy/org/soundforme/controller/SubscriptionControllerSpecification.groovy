package org.soundforme.controller

import org.soundforme.config.SharedConfig
import org.soundforme.external.DiscogsConnectionException
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.soundforme.repositories.ReleaseRepository
import org.soundforme.repositories.SubscriptionRepository
import org.soundforme.service.SubscriptionService
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.inject.Inject

import static org.assertj.core.api.Assertions.assertThat
import static org.hamcrest.Matchers.hasSize
import static org.soundforme.service.EntityObjectsBuilder.createRandomRelease
import static org.soundforme.service.EntityObjectsBuilder.createRandomSubscription
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
    @Inject
    def SubscriptionRepository subscriptionRepository
    @Inject
    def ReleaseRepository releaseRepository;

    def MockMvc mockMvc
    def subscriptionService = Mock(SubscriptionService)

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    def "controller should return new subscription with 201 code if label or artist exists in discogs or in db"() {
        setup:
        subscriptionController.subscriptionService = subscriptionService

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
        setup:
        subscriptionController.subscriptionService = subscriptionService

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
        setup:
        subscriptionController.subscriptionService = subscriptionService

        when:
        def response = mockMvc.perform(post("/subscriptions")
                .param("discogsStringId", "a100")
        )

        then:
        1 * subscriptionService.follow("a100") >> {throw new DiscogsConnectionException()}
        response.andExpect(status().isBadRequest())
                .andExpect(status().reason("Error on connection to discogs"))
    }

    def "controller should return releases of passed subscription"() {
        setup:
        def releases = [
                releaseRepository.save(createRandomRelease(100)),
                releaseRepository.save(createRandomRelease(200)),
                releaseRepository.save(createRandomRelease(300))
        ]
        def subscription = createRandomSubscription(true, 500, false)
        subscription.releases = releases[0..1]
        subscription = subscriptionRepository.save(subscription)

        when:
        def response = mockMvc.perform(get("/subscriptions/{id}/releases", subscription.id))

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').value(hasSize(2)))
                .andExpect(jsonPath('$[0].discogsId').value(100))
                .andExpect(jsonPath('$[1].discogsId').value(200))

    }

    def "controller should return empty array as result of getting releases of empty subscription"() {
        setup:
        def subscription = subscriptionRepository.save(createRandomSubscription(true, 500, false))

        when:
        def response = mockMvc.perform(get("/subscriptions/{id}/releases", subscription.id))

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').value(hasSize(0)))
    }

    def "controller should return NOT_FOUND error if subscription not found"() {
        when:
        def response = mockMvc.perform(get("/subscriptions/1/releases"))

        then:
        response.andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("message").value("Subscription 1 not found"))
    }

    def "controller should return all not closed subscriptions"() {
        setup:
        subscriptionRepository.save(createRandomSubscription(true, 500, false))
        subscriptionRepository.save(createRandomSubscription(false, 501, false))
        subscriptionRepository.save(createRandomSubscription(true, 502, true))

        when:
        def response = mockMvc.perform(get("/subscriptions") )

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').value(hasSize(2)))
                .andExpect(jsonPath('$[0].discogsId').value(500))
                .andExpect(jsonPath('$[0].closed').value(false))
                .andExpect(jsonPath('$[0].type').value("LABEL"))
                .andExpect(jsonPath('$[1].discogsId').value(501))
                .andExpect(jsonPath('$[1].closed').value(false))
                .andExpect(jsonPath('$[1].type').value("ARTIST"))
    }

    def "controller should return all empty list if no subscriptions"() {
        when:
        def response = mockMvc.perform(get("/subscriptions"))

        then:
        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$').isArray())
                .andExpect(jsonPath('$').value(hasSize(0)))
    }

    def "unsubscribe should not work with not existed entities, NOT_FOUND status expected"() {
        when:
        def response = mockMvc.perform(delete("/subscriptions/wrongID"))

        then:
        response.andExpect(status().isNotFound())
                .andExpect(status().reason("Entity does not exist in database"))
    }

    def "unsubscribe should change status of subscription to closed"() {
        setup:
        def subscription = subscriptionRepository.save(createRandomSubscription(true, 500, false))

        when:
        def response = mockMvc.perform(delete("/subscriptions/{id}", subscription.id))

        then:
        response.andExpect(status().isOk())
        assertThat(subscriptionRepository.findOne(subscription.id).closed).isTrue()
    }
    
    void cleanup() {
        subscriptionRepository.deleteAll()
        releaseRepository.deleteAll()
    }
}
