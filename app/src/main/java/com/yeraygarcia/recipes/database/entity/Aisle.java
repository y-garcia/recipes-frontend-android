package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

@Entity(
        tableName = "aisle",
        indices = {@Index(value = "name", unique = true)}
)
public class Aisle {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    private String name;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Aisle(@NonNull String name) {
        this.id = UUIDTypeConverter.newUUID();
        this.name = name;
    }

    public Aisle(@NonNull UUID id, @NonNull String name) {
        this.id = id;
        this.name = name;
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
}
