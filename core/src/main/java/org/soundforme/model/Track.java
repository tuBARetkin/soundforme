package org.soundforme.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NGorelov
 */
public class Track {
    private String title;
    private String duration;
    private String position;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
