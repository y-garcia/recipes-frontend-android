package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity(tableName = "recipe")
public class Recipe {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    private String name;

    @NonNull
    private Integer portions;

    private Integer duration;

    private String url;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Recipe(@NonNull String name, @NonNull Integer portions, Integer duration, String url) {
        this.id = UUIDTypeConverter.newUUID();
        this.name = name;
        this.portions = portions;
        this.duration = duration;
        this.url = url;
    }

    public Recipe(@NonNull UUID id, @NonNull String name, @NonNull Integer portions, Integer duration, String url) {
        this.id = id;
        this.name = name;
        this.portions = portions;
        this.duration = duration;
        this.url = url;
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
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public Integer getPortions() {
        return portions;
    }

    public void setPortions(@NonNull Integer portions) {
        this.portions = portions;
    }

    public Integer getDuration() {
        return duration;
    }

    public Long getDurationInMinutes() {
        if (getDuration() == null) {
            return 0L;
        }
        return TimeUnit.SECONDS.toMinutes(getDuration());
    }

    public void setDuration(Integer duration) {
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
        return "\n[Recipe: " + name +
                ", Portions: " + portions +
                ", Duration: " + getDurationInMinutes() + " minutes" +
                ", Source: " + (url != null ? url : "(none)]");
    }

    public void increasePortions() {
        this.portions += 1;
    }

    public void decreasePortions() {
        this.portions -= 1;
    }

    public String getFormattedDuration(Context context) {
        return context.getString(R.string.duration_format, String.format(Locale.getDefault(), "%d", getDurationInMinutes()));
    }
}
