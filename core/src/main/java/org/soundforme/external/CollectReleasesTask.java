package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.model.ReleasesPage;
import org.soundforme.model.Release;
import org.soundforme.model.SubscriptionType;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author NGorelov
 */
public class CollectReleasesTask extends RecursiveTask<Set<Release>> {
    private static final Logger logger = LoggerFactory.getLogger(CollectReleasesTask.class);

    private final ReleasesPage releasesPage;
    private final Set<Integer> ignoredReleases;
    private final DiscogsStore discogsStore;
    private final SubscriptionType subscriptionType;
    private final int discogsId;

    public CollectReleasesTask(ReleasesPage releasesPage,
                               Set<Integer> ignoredReleases,
                               DiscogsStore discogsStore,
                               SubscriptionType subscriptionType,
                               int discogsId) {
        this.releasesPage = releasesPage;
        this.ignoredReleases = ignoredReleases;
        this.discogsStore = discogsStore;
        this.subscriptionType = subscriptionType;
        this.discogsId = discogsId;
    }



    @Override
    protected Set<Release> compute() {
        int currentPage = releasesPage.getPagination().getPage();
        int totalPages = releasesPage.getPagination().getPages();
        logger.debug("Start loading {} page of resource {}/{}", currentPage, discogsId, subscriptionType);

        CollectReleasesTask releasesFromNextPage = null;
        if (currentPage < totalPages) {
            releasesFromNextPage = buildTaskForNextPage(currentPage);
            releasesFromNextPage.fork();
        }

        List<LoadReleaseTask> releasesFromThisPage = releasesPage.getReleases().stream()
                .filter(releaseLink -> !ignoredReleases.contains(releaseLink.getId()))
                .map(releaseLink -> {
                    LoadReleaseTask task = new LoadReleaseTask(releaseLink.getId(), discogsStore);
                    task.fork();
                    return task;
                })
                .collect(Collectors.toList());

        Set<Release> result = releasesFromThisPage.stream()
                .map(LoadReleaseTask::join)
                .collect(Collectors.toSet());

        if(releasesFromNextPage != null){
            result.addAll(releasesFromNextPage.join());
        }

        return result;
    }

    private CollectReleasesTask buildTaskForNextPage(int currentPage){
        try {
            return new CollectReleasesTask(
                    getReleasesPage(subscriptionType, discogsId, currentPage + 1).get(),
                    ignoredReleases,
                    discogsStore,
                    subscriptionType,
                    discogsId
            );
        } catch (InterruptedException | ExecutionException e){
            throw new ReleaseCollectingException("Error on collecting releases pages", e);
        }
    }

    private Future<ReleasesPage> getReleasesPage(SubscriptionType type, int id, int page) {
        if (type == SubscriptionType.ARTIST) {
            return discogsStore.getArtistReleasesPage(id, page);
        } else {
            return discogsStore.getLabelReleasesPage(id, page);
        }
    }
}
