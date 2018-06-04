package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(
        tableName = "placement",
        indices = {
                @Index(value = {"aisle_id", "store_id"}, unique = true),
                @Index("aisle_id"),
                @Index("store_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Aisle.class, parentColumns = "id", childColumns = "aisle_id"),
                @ForeignKey(entity = Store.class, parentColumns = "id", childColumns = "store_id")
        }
)
public class Placement {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "aisle_id")
    private long aisleId;

    @ColumnInfo(name = "store_id")
    private long storeId;

    @ColumnInfo(name = "sort_order")
    private int sortOrder;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public Placement(long aisleId, long storeId, int sortOrder) {
        this.aisleId = aisleId;
        this.storeId = storeId;
        this.sortOrder = sortOrder;
    }

    public Placement(long id, long aisleId, long storeId, int sortOrder) {
        this.id = id;
        this.aisleId = aisleId;
        this.storeId = storeId;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAisleId() {
        return aisleId;
    }

    public void setAisleId(long aisleId) {
        this.aisleId = aisleId;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
