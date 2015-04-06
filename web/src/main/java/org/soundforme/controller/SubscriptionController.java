package org.soundforme.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.service.SubscriptionService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createNew(@RequestParam("discogsStringId") String discogsStringId) {
        ResponseEntity<?> result;

        try {
            Subscription subscription = subscriptionService.follow(discogsStringId);
            if(subscription != null) {
                result = new ResponseEntity<>(subscription, HttpStatus.CREATED);
            } else {
                result = new ResponseEntity<>("Subscription " + discogsStringId + " not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Throwable e) {
            logger.warn("Error on loading subscription {}", discogsStringId, e);
            result = new ResponseEntity<>("Error on loading subscription " + discogsStringId, HttpStatus.BAD_REQUEST);
        }

        return result;
    }
}
