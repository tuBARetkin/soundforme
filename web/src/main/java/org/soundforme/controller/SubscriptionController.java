package org.soundforme.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.service.SubscriptionService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

/**
 * @author NGorelov
 */
@RestController
public class SubscriptionController {
    private final static Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Inject
    private SubscriptionService subscriptionService;

    @RequestMapping(value = "/subscriptions", method = RequestMethod.GET)
    public List<Subscription> findAll() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/subscriptions/{id}/releases", method = RequestMethod.GET)
    public List<Release> findReleases(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
    public Subscription createNew(@RequestParam("discogsStringId") String discogsStringId) {
        throw new UnsupportedOperationException();
    }
}
