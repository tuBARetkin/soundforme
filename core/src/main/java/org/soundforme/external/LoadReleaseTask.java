package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.model.ArtistExternal;
import org.soundforme.external.model.LabelExternal;
import org.soundforme.external.model.ReleaseExternal;
import org.soundforme.model.Release;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author NGorelov
 */
public class LoadReleaseTask extends RecursiveTask<Release> {
    private static final Logger logger = LoggerFactory.getLogger(LoadReleaseTask.class);

    private final int discogsId;
    private final DiscogsStore discogsStore;

    public LoadReleaseTask(int discogsId, DiscogsStore discogsStore) {
        this.discogsId = discogsId;
        this.discogsStore = discogsStore;
    }

    @Override
    protected Release compute() {
        try {
            ReleaseExternal releaseExternal = discogsStore.getReleaseResource(discogsId).get();

            Release result = new Release();
            result.setAdditionDate(LocalDateTime.now());
            result.setArtist(
                    releaseExternal.getArtists().stream()
                    .map(ArtistExternal::getName)
                    .collect(Collectors.joining(", "))
            );
            result.setCatNo(
                    releaseExternal.getLabels().stream()
                    .map(LabelExternal::getCatNo)
                    .collect(Collectors.joining(", "))
            );
            result.setChecked(false);
            result.setReleaseDate(releaseExternal.getDate());
            result.setTitle(releaseExternal.getTitle());

            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new ReleaseCollectingException("Error on collecting single releases", e);
        }
    }
}
