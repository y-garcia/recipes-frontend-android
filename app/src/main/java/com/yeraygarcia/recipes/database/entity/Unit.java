package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

@Entity(
        tableName = "unit",
        indices = {
                @Index(value = "name_singular", unique = true),
                @Index(value = "name_plural", unique = true)
        }
)
public class Unit {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    @ColumnInfo(name = "name_singular")
    @SerializedName("name_singular")
    private String nameSingular;

    @NonNull
    @ColumnInfo(name = "name_plural")
    @SerializedName("name_plural")
    private String namePlural;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Unit(@NonNull String nameSingular, @NonNull String namePlural) {
        this.id = UUIDTypeConverter.newUUID();
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
    }

    public Unit(@NonNull UUID id, @NonNull String nameSingular, @NonNull String namePlural) {
        this.id = id;
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
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
    public String getNameSingular() {
        return nameSingular;
    }

    public void setNameSingular(@NonNull String nameSingular) {
        this.nameSingular = nameSingular;
    }

    @NonNull
    public String getNamePlural() {
        return namePlural;
    }

    public void setNamePlural(@NonNull String namePlural) {
        this.namePlural = namePlural;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id=" + id +
                ", nameSingular='" + nameSingular + '\'' +
                ", namePlural='" + namePlural + '\'' +
                '}';
    }
}
