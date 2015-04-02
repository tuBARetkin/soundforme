package org.soundforme.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author NGorelov
 */
@RestController
public class ReleaseController {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);

    @RequestMapping(value = "/releases", method = RequestMethod.GET)
    public List<Release> findAll(@RequestBody(required = false) Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/releases/{id}", method = RequestMethod.GET)
    public Release findOne(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/releases/{id}", method = RequestMethod.PUT)
    public void update(@RequestBody Release release, @PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }
}
