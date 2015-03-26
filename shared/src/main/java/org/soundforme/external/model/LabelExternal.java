package org.soundforme.external.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author NGorelov
 */
public class LabelExternal {
    private Integer id;
    @SerializedName("catno")
    private String catNo;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCatNo() {
        return catNo;
    }

    public void setCatNo(String catNo) {
        this.catNo = catNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
