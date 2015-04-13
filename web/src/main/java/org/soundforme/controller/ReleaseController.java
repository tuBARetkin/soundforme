package org.soundforme.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.service.ReleaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

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
    public Page<Release> findAll(@RequestParam("page") int page, @RequestParam("size") int size) {
        return releaseService.loadPage(new PageRequest(page, size));
    }

    @RequestMapping(value = "/releases/{id}", method = RequestMethod.PUT)
    public void markStarredOrChecked(@RequestBody Release release, @PathVariable("id") String id) {
        if(release.getChecked() != null) {
            releaseService.setChecked(id, release.getChecked());
        }
        if(release.getStarred() != null) {
            releaseService.setStarred(id, release.getStarred());
        }
    }
}
