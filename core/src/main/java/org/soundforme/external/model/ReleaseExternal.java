package org.soundforme.external.model;

import com.google.gson.annotations.SerializedName;
import org.soundforme.model.Track;

import java.util.List;
import java.util.Set;

/**
 * @author NGorelov
 */
public class ReleaseExternal {
    private Integer id;
    private String title;
    private Set<ArtistExternal> artists;
    private Set<LabelExternal> labels;
    @SerializedName("released")
    private String date;
    private List<Track> tracklist;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<ArtistExternal> getArtists() {
        return artists;
    }

    public void setArtists(Set<ArtistExternal> artists) {
        this.artists = artists;
    }

    public Set<LabelExternal> getLabels() {
        return labels;
    }

    public void setLabels(Set<LabelExternal> labels) {
        this.labels = labels;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Track> getTracklist() {
        return tracklist;
    }

    public void setTracklist(List<Track> tracklist) {
        this.tracklist = tracklist;
    }
}
