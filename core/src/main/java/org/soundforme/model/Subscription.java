package org.soundforme.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

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
    private List<Integer> collectedReleases;
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

    public List<Integer> getCollectedReleases() {
        return collectedReleases;
    }

    public void setCollectedReleases(List<Integer> collectedReleases) {
        this.collectedReleases = collectedReleases;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }
}
