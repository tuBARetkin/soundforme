package org.soundforme.collector.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Set;

/**
 * @author NGorelov
 */
public class ReleaseExternal {
    private Integer id;
    private String title;
    private Set<Artist> artists;
    @SerializedName("released")
    private String date;
    private Set<TrackExternal> tracklist;

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

    public Set<Artist> getArtists() {
        return artists;
    }

    public void setArtists(Set<Artist> artists) {
        this.artists = artists;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Set<TrackExternal> getTracklist() {
        return tracklist;
    }

    public void setTracklist(Set<TrackExternal> tracklist) {
        this.tracklist = tracklist;
    }
}
