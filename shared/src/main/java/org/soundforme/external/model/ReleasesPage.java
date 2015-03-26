package org.soundforme.external.model;

import java.util.List;

/**
 * @author NGorelov
 */
public class ReleasesPage {
    private Pagination pagination;
    private List<ReleaseLink> releases;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<ReleaseLink> getReleases() {
        return releases;
    }

    public void setReleases(List<ReleaseLink> releases) {
        this.releases = releases;
    }
}
