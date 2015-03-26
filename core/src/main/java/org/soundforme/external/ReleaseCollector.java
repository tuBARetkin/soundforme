package org.soundforme.external;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.model.ReleaseExternal;
import org.soundforme.external.model.ReleaseLink;
import org.soundforme.external.model.ReleasesPage;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.model.SubscriptionType;
import org.soundforme.repositories.ReleaseRepository;
import org.soundforme.repositories.SubscriptionRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * @author NGorelov
 */
@Component
public class ReleaseCollector {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseCollector.class);

    @Inject
    private DiscogsStore discogsStore;
    @Inject
    private ReleaseRepository releaseRepository;

    public Set<Release> collectAll(Subscription subscription){
        checkNotNull(subscription, "subscription should be defined");
        checkArgument(subscription.getType() != null, "type of subscription should be defined");
        checkArgument(subscription.getDiscogsId() != null, "subscription id from discogs should be defined");

        ReleasesPage firstPage;

        try {
            if (subscription.getType() == SubscriptionType.ARTIST) {
                firstPage = discogsStore.getArtistReleasesPage(subscription.getDiscogsId(), 1).get();
            } else {
                firstPage = discogsStore.getLabelReleasesPage(subscription.getDiscogsId(), 1).get();
            }
        }catch (InterruptedException | ExecutionException e) {
            throw new ReleaseCollectingException("Error on loading first page of subscription", e);
        }

        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        return forkJoinPool.invoke(new CollectReleasesTask(
                firstPage,
                subscription.getCollectedReleases() == null ? newHashSet() : subscription.getCollectedReleases(),
                discogsStore,
                subscription.getType(),
                subscription.getDiscogsId())
        );
    }
}
