package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

@Entity(tableName = "recipe")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    private int portions;

    private int duration;

    private String url;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Recipe(@NonNull String name, int portions, int duration, String url) {
        this.name = name;
        this.portions = portions;
        this.duration = duration;
        this.url = url;
    }

    public Recipe(long id, @NonNull String name, int portions, int duration, String url) {
        this.id = id;
        this.name = name;
        this.portions = portions;
        this.duration = duration;
        this.url = url;
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

    public int getPortions() {
        return portions;
    }

    public void setPortions(int portions) {
        this.portions = portions;
    }

    public int getDuration() {
        return duration;
    }

    private long getDurationInMinutes() {
        return TimeUnit.SECONDS.toMinutes(getDuration());
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Methods /////////////////////////////////////////////////////////////////////////


    @Override
    public String toString() {
        return "Recipe: " + getName() +
                "\nPortions: " + getPortions() +
                "\nDuration: " + getDurationInMinutes() + " minutes" +
                "\nSource: " + (getUrl() != null ? getUrl() : "-");
    }
}
