package org.soundforme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.repositories.SubscriptionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author NGorelov
 */
@Service
public class ReleaseService {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseService.class);

    public List<Release> loadPage(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    public Release findOne(String id) {
        throw new UnsupportedOperationException();
    }

    public void markChecked(Release release) {
        throw new UnsupportedOperationException();
    }

    public void markStarred(Release release) {
        throw new UnsupportedOperationException();
    }
}
