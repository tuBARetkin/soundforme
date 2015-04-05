package org.soundforme.controller

import org.soundforme.service.ReleaseService
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

/**
 * @author NGorelov
 */
class ReleaseControllerSpecification extends Specification {
    def releaseService = Mock(ReleaseService)
    def releaseController = new ReleaseController(releaseService: releaseService)
    def mockMvc = MockMvcBuilders.standaloneSetup(releaseController).build()


}
