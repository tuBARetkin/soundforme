package org.soundforme.controller;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.repositories.SubscriptionRepository;
import org.soundforme.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author NGorelov
 */
@RestController
public class SubscriptionController {
    private final static Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Inject
    private SubscriptionService subscriptionService;
    @Inject
    private SubscriptionRepository subscriptionRepository;

    @RequestMapping(value = "/subscriptions", method = RequestMethod.GET)
    public List<Subscription> findAll() {
        return subscriptionService.findAll();
    }

    @RequestMapping(value = "/subscriptions/{id}/releases", method = RequestMethod.GET)
    public ResponseEntity<?> findReleases(@PathVariable("id") String id) {
        List<Release> releases = newArrayList();
        ResponseEntity<?> result = new ResponseEntity<>(releases, HttpStatus.OK);

        Subscription subscription = subscriptionRepository.findOne(id);
        if(subscription != null) {
            logger.debug("Subscription {} found. Preparing OK response", id);
            if (subscription.getReleases() != null) {
                releases.addAll(subscription.getReleases());
            }
        } else {
            logger.debug("Subscription {} not found. Preparing NOT_FOUND response", id);
            Map<String, Object> errorObject = ImmutableMap.of("message", "Subscription " + id + " not found");
            result = new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
        }

        return result;
    }

    @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
    public ResponseEntity<?> createNew(@RequestParam("discogsStringId") String discogsStringId) {
        ResponseEntity<?> result;

        Subscription subscription = subscriptionService.follow(discogsStringId);
        if(subscription != null) {
            logger.debug("Subscription {} found. Preparing CREATED response", discogsStringId);
            result = new ResponseEntity<>(subscription, HttpStatus.CREATED);
        } else {
            logger.debug("Subscription {} not found in discogs database. Preparing BAD_REQUEST response", discogsStringId);
            Map<String, Object> errorObject = ImmutableMap.of("message", "Subscription " + discogsStringId + " not found");
            result = new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
        }

        return result;
    }

    @RequestMapping(value = "/subscriptions/{id}", method = RequestMethod.DELETE)
    public void unsubscribe(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }
}
