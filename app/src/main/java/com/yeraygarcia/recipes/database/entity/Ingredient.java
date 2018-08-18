package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

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

    private static final UUID DEFAULT_AISLE_ID = UUID.fromString("7f8a3138-a072-11e8-9ac4-0a0027000012");

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    private String name;

    @ColumnInfo(name = "aisle_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("aisle_id")
    private UUID aisleId;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Ingredient(@NonNull String name) {
        this.id = UUIDTypeConverter.newUUID();
        this.name = name;
        this.aisleId = DEFAULT_AISLE_ID;
    }

    @Ignore
    public Ingredient(@NonNull String name, UUID aisleId) {
        this.id = UUIDTypeConverter.newUUID();
        this.name = name;
        this.aisleId = aisleId;
    }

    public Ingredient(@NonNull UUID id, @NonNull String name, UUID aisleId) {
        this.id = id;
        this.name = name;
        this.aisleId = aisleId;
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

    public UUID getAisleId() {
        return aisleId;
    }

    public void setAisleId(UUID aisleId) {
        this.aisleId = aisleId;
    }
}
