package org.soundforme.external.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author NGorelov
 */
public class ReleaseLink {
    private Integer id;
    @SerializedName("resource_url")
    private String resourceUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }
}
