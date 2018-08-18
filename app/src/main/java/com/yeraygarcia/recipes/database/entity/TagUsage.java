package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

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

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    private Long created;

    @NonNull
    @ColumnInfo(name = "tag_id", typeAffinity = ColumnInfo.BLOB)
    private UUID tagId;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public TagUsage(@NonNull UUID tagId) {
        this.id = UUIDTypeConverter.newUUID();
        this.created = System.currentTimeMillis() / 1000L;
        this.tagId = tagId;
    }

    public TagUsage(@NonNull UUID id, @NonNull UUID tagId) {
        this.id = id;
        this.created = System.currentTimeMillis() / 1000L;
        this.tagId = tagId;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
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
    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(@NonNull UUID tagId) {
        this.tagId = tagId;
    }
}
