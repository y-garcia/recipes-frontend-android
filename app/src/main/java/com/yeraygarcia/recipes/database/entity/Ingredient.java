package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.RESTRICT;

@Entity(
        tableName = "ingredient",
        indices = {
                @Index(value = "name", unique = true),
                @Index("aisle_id")
        },
        foreignKeys = {@ForeignKey(entity = Aisle.class, parentColumns = "id", childColumns = "aisle_id", onDelete = RESTRICT)}
)
public class Ingredient {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    @ColumnInfo(name = "aisle_id")
    @SerializedName("aisle_id")
    private long aisleId;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Ingredient(@NonNull String name, long aisleId) {
        this.name = name;
        this.aisleId = aisleId;
    }

    public Ingredient(long id, @NonNull String name, long aisleId) {
        this.id = id;
        this.name = name;
        this.aisleId = aisleId;
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

    public long getAisleId() {
        return aisleId;
    }

    public void setAisleId(long aisleId) {
        this.aisleId = aisleId;
    }
}
