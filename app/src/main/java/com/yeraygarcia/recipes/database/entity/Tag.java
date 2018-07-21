package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(
        tableName = "tag",
        indices = {
                @Index(value = "name", unique = true)
        }
)
public class Tag {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private long id;

    @NonNull
    @SerializedName("name")
    private String name;

    @NonNull
    @ColumnInfo(name = "usage_count")
    @SerializedName("usage_count")
    private Long usageCount;

    @NonNull
    @ColumnInfo(name = "last_used")
    @SerializedName("last_used")
    private Long lastUsed;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Tag(@NonNull String name, @NonNull Long usageCount) {
        this.name = name;
        this.usageCount = usageCount;
        this.lastUsed = System.currentTimeMillis() / 1000L;
    }

    public Tag(long id, @NonNull String name, @NonNull Long usageCount) {
        this.id = id;
        this.name = name;
        this.usageCount = usageCount;
        this.lastUsed = System.currentTimeMillis() / 1000L;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(@NonNull Long usageCount) {
        this.usageCount = usageCount;
    }

    @NonNull
    public Long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(@NonNull Long lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public String toString() {
        return "\n[Tag: " + name +
                ", id: " + id +
                ", usageCount: " + usageCount +
                ", lastUsed: " + new java.util.Date(lastUsed * 1000) + "]";
    }
}
