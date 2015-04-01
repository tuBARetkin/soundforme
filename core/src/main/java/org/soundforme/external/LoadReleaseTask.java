package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.model.ArtistExternal;
import org.soundforme.external.model.LabelExternal;
import org.soundforme.external.model.ReleaseExternal;
import org.soundforme.model.Release;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
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
            logger.debug("Start loading release {} from discogs", discogsId);
            ReleaseExternal releaseExternal = discogsStore.getReleaseResource(discogsId).get();
            logger.debug("Release {} collected from discogs", discogsId);

            Release result = new Release();
            result.setCollectedDate(LocalDateTime.now());
            result.setArtist(
                    releaseExternal.getArtists().stream()
                            .map(ArtistExternal::getName)
                            .collect(Collectors.joining(", "))
            );
            result.setDiscogsId(discogsId);
            result.setCatNo(
                    releaseExternal.getLabels().stream()
                            .map((extLabel) -> extLabel.getCatNo().toUpperCase().trim().replace(" ", ""))
                            .collect(Collectors.joining(", "))
            );
            result.setLabel(
                    releaseExternal.getLabels().stream()
                            .map(LabelExternal::getName)
                            .collect(Collectors.joining(", "))
            );
            result.setTrackList(
                    releaseExternal.getTracklist().stream()
                            .filter((track) -> !track.getTitle().isEmpty())
                            .collect(Collectors.toList())
            );
            result.setChecked(false);
            result.setStarred(false);
            result.setReleaseDate(releaseExternal.getDate());
            result.setTitle(releaseExternal.getTitle());

            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new ReleaseCollectingException("Error on collecting single releases", e);
        }
    }
}
