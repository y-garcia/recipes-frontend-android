package com.yeraygarcia.recipes.database.entity.custom;

import android.arch.persistence.room.ColumnInfo;

public class UiTag {

    @ColumnInfo(name = "tag_name")
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
