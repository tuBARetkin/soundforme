package org.soundforme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.repositories.SubscriptionRepository;
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

    @Inject
    private SubscriptionRepository subscriptionRepository;

    public List<Release> findDistinct(Set<Subscription> subscriptions) {
        throw new UnsupportedOperationException();
    }

    public void save(Subscription subscription, Set<Release> releases) {
        throw new UnsupportedOperationException();
    }

    public void markAsSeen(Release release) {
        throw new UnsupportedOperationException();
    }
}
