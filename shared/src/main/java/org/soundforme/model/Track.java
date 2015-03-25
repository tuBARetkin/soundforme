package org.soundforme.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NGorelov
 */
public class Track {
    private String title;
    private String length;
    private String number;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
