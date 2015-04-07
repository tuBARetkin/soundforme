package org.soundforme.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.service.ReleaseService;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

/**
 * @author NGorelov
 */
@RestController
public class ReleaseController {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseController.class);

    @Inject
    private ReleaseService releaseService;

    @RequestMapping(value = "/releases", method = RequestMethod.GET)
    public List<Release> findAll(@RequestBody(required = false) Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/releases/{id}", method = RequestMethod.PUT)
    public void markStarredOrChecked(@RequestBody Release release, @PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }
}
