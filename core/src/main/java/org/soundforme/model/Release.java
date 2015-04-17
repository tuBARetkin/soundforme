package org.soundforme.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author NGorelov
 */
@Document
public class Release {
    @Id
    private String id;
    @Indexed @Field("dI")
    private Integer discogsId;
    @Field("a")
    private String artist;
    @Field("t")
    private String title;
    @Field("rD")
    private String releaseDate;
    @Field("cD")
    private LocalDateTime collectedDate;
    @Field("l")
    private String label;
    @Field("cN")
    private String catNo;
    @Field("c")
    private Boolean checked;
    @Indexed @Field("s")
    private Boolean starred;
    @Field("tL")
    private List<Track> trackList;

    public String getId() {
        return id;
    }

    public Integer getDiscogsId() {
        return discogsId;
    }

    public void setDiscogsId(Integer discogsId) {
        this.discogsId = discogsId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getCollectedDate() {
        return collectedDate;
    }

    public void setCollectedDate(LocalDateTime collectedDate) {
        this.collectedDate = collectedDate;
    }

    public String getCatNo() {
        return catNo;
    }

    public void setCatNo(String catNo) {
        this.catNo = catNo;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Release release = (Release) o;

        if (!artist.equals(release.artist)) return false;
        if (catNo != null ? !catNo.equals(release.catNo) : release.catNo != null) return false;
        if (checked != null ? !checked.equals(release.checked) : release.checked != null) return false;
        if (!collectedDate.equals(release.collectedDate)) return false;
        if (!discogsId.equals(release.discogsId)) return false;
        if (id != null ? !id.equals(release.id) : release.id != null) return false;
        if (label != null ? !label.equals(release.label) : release.label != null) return false;
        if (releaseDate != null ? !releaseDate.equals(release.releaseDate) : release.releaseDate != null) return false;
        if (starred != null ? !starred.equals(release.starred) : release.starred != null) return false;
        if (!title.equals(release.title)) return false;
        if (trackList != null ? !trackList.equals(release.trackList) : release.trackList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = discogsId.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (releaseDate != null ? releaseDate.hashCode() : 0);
        result = 31 * result + collectedDate.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (catNo != null ? catNo.hashCode() : 0);
        result = 31 * result + (checked != null ? checked.hashCode() : 0);
        result = 31 * result + (starred != null ? starred.hashCode() : 0);
        result = 31 * result + (trackList != null ? trackList.hashCode() : 0);
        return result;
    }
}
