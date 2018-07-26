package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(
        tableName = "last_update"
)
public class LastUpdate {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "last_update")
    private Long lastUpdate;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    public LastUpdate(@NonNull Long lastUpdate) {
        this.id = 1;
        this.lastUpdate = lastUpdate;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(@NonNull Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
