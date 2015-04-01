package org.soundforme.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author NGorelov
 */
@Document
public class Subscription {
    @Id
    private String id;

    private String title;
    private SubscriptionType type;
    private Integer discogsId;
    private Set<Integer> collectedReleases;
    private Boolean closed;

    @DBRef
    private Set<Release> releases;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public Integer getDiscogsId() {
        return discogsId;
    }

    public void setDiscogsId(Integer discogsId) {
        this.discogsId = discogsId;
    }

    public Set<Release> getReleases() {
        return releases;
    }

    public void setReleases(Set<Release> releases) {
        this.releases = releases;
    }

    public void addRelease(Release release){
        if(release != null){
            if(releases == null){
                releases = newHashSet();
            }
            releases.add(release);
        }
    }

    public Set<Integer> getCollectedReleases() {
        return collectedReleases;
    }

    public void setCollectedReleases(Set<Integer> collectedReleases) {
        this.collectedReleases = collectedReleases;
    }

    public void addCollectedRelease(int id){
        if(id > 0){
            if(collectedReleases == null){
                collectedReleases = newHashSet();
            }
            collectedReleases.add(id);
        }
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (closed != null ? !closed.equals(that.closed) : that.closed != null) return false;
        if (!discogsId.equals(that.discogsId)) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!title.equals(that.title)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + discogsId.hashCode();
        result = 31 * result + (closed != null ? closed.hashCode() : 0);
        return result;
    }
}
