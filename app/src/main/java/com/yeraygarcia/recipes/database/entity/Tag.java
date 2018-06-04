package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(
        tableName = "tag",
        indices = {
                @Index(value = "name", unique = true)
        }
)
public class Tag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Tag(@NonNull String name) {
        this.name = name;
    }

    public Tag(long id, @NonNull String name) {
        this.id = id;
        this.name = name;
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
}
