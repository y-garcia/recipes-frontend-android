package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "usage_tag",
        indices = {
                @Index("tag_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tag_id", onDelete = CASCADE)
        }
)
public class TagUsage {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private Long created;

    @NonNull
    @ColumnInfo(name = "tag_id")
    private Long tagId;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public TagUsage(@NonNull Long tagId) {
        this.created = System.currentTimeMillis() / 1000L;
        this.tagId = tagId;
    }

    public TagUsage(long id, @NonNull Long tagId) {
        this.id = id;
        this.created = System.currentTimeMillis() / 1000L;
        this.tagId = tagId;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public Long getCreated() {
        return created;
    }

    public void setCreated(@NonNull Long created) {
        this.created = created;
    }

    @NonNull
    public Long getTagId() {
        return tagId;
    }

    public void setTagId(@NonNull Long tagId) {
        this.tagId = tagId;
    }
}
