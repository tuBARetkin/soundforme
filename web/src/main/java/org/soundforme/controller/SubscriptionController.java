package org.soundforme.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author NGorelov
 */
@RestController
public class SubscriptionController {
    private final static Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @RequestMapping(value = "/subscriptions", method = RequestMethod.GET)
    public List<Release> findAll() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/subscriptions/{id}/releases", method = RequestMethod.GET)
    public Release findOne(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }
}
