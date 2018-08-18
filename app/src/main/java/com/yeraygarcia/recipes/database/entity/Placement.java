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

import static android.arch.persistence.room.ForeignKey.RESTRICT;

@Entity(
        tableName = "placement",
        indices = {
                @Index(value = {"aisle_id", "store_id"}, unique = true),
                @Index("aisle_id"),
                @Index("store_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Aisle.class, parentColumns = "id", childColumns = "aisle_id", onDelete = RESTRICT),
                @ForeignKey(entity = Store.class, parentColumns = "id", childColumns = "store_id", onDelete = RESTRICT)
        }
)
public class Placement {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    @ColumnInfo(name = "aisle_id", typeAffinity = ColumnInfo.BLOB)
    private UUID aisleId;

    @NonNull
    @ColumnInfo(name = "store_id", typeAffinity = ColumnInfo.BLOB)
    private UUID storeId;

    @ColumnInfo(name = "sort_order")
    private int sortOrder;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Placement(@NonNull UUID aisleId, @NonNull UUID storeId, int sortOrder) {
        this.id = UUIDTypeConverter.newUUID();
        this.aisleId = aisleId;
        this.storeId = storeId;
        this.sortOrder = sortOrder;
    }

    public Placement(@NonNull UUID id, @NonNull UUID aisleId, @NonNull UUID storeId, int sortOrder) {
        this.id = id;
        this.aisleId = aisleId;
        this.storeId = storeId;
        this.sortOrder = sortOrder;
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
    public UUID getAisleId() {
        return aisleId;
    }

    public void setAisleId(@NonNull UUID aisleId) {
        this.aisleId = aisleId;
    }

    @NonNull
    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(@NonNull UUID storeId) {
        this.storeId = storeId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
