package org.soundforme.collector.model;

import java.util.List;

/**
 * @author NGorelov
 */
public class ArtistReleasesPage extends PaginatedResource {
    private List<ArtistReleaseLink> releases;

    public List<ArtistReleaseLink> getReleases() {
        return releases;
    }

    public void setReleases(List<ArtistReleaseLink> releases) {
        this.releases = releases;
    }
}
