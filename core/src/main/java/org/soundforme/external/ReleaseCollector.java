package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.model.ReleasesPage;
import org.soundforme.model.Release;
import org.soundforme.model.Subscription;
import org.soundforme.model.SubscriptionType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author NGorelov
 */
@Component
public class ReleaseCollector {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseCollector.class);

    @Inject
    private DiscogsStore discogsStore;

    public Set<Release> collectAll(Subscription subscription){
        checkNotNull(subscription, "subscription should be defined");
        checkArgument(subscription.getType() != null, "type of subscription should be defined");
        checkArgument(subscription.getDiscogsId() != null, "subscription id from discogs should be defined");
        logger.info("Collecting releases of subscription {}/{} started", subscription.getDiscogsId(), subscription.getType());

        ReleasesPage firstPage;
        try {
            if (subscription.getType() == SubscriptionType.ARTIST) {
                firstPage = discogsStore.getArtistReleasesPage(subscription.getDiscogsId(), 1).get();
            } else {
                firstPage = discogsStore.getLabelReleasesPage(subscription.getDiscogsId(), 1).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ReleaseCollectingException("Error on loading first page of subscription", e);
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        return forkJoinPool.invoke(new CollectReleasesTask(
                firstPage,
                subscription.getCollectedReleases() == null ? newHashSet() : subscription.getCollectedReleases(),
                discogsStore,
                subscription.getType(),
                subscription.getDiscogsId())
        );
    }
}
