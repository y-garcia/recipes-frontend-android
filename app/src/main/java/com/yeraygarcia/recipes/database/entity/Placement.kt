package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.RESTRICT
import com.yeraygarcia.recipes.database.UUIDTypeConverter
import java.util.*

@Entity(
    tableName = "placement",
    indices = [Index(
        value = arrayOf("aisle_id", "store_id"),
        unique = true
    ), Index("aisle_id"), Index("store_id")],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Aisle::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("aisle_id"),
            onDelete = RESTRICT
        ),
        ForeignKey(
            entity = Store::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("store_id"),
            onDelete = RESTRICT
        )
    )
)
class Placement {

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID

    @ColumnInfo(name = "aisle_id", typeAffinity = ColumnInfo.BLOB)
    var aisleId: UUID

    @ColumnInfo(name = "store_id", typeAffinity = ColumnInfo.BLOB)
    var storeId: UUID

    @ColumnInfo(name = "sort_order")
    var sortOrder: Int = 0

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    constructor(aisleId: UUID, storeId: UUID, sortOrder: Int) {
        this.id = UUIDTypeConverter.newUUID()
        this.aisleId = aisleId
        this.storeId = storeId
        this.sortOrder = sortOrder
    }

    constructor(id: UUID, aisleId: UUID, storeId: UUID, sortOrder: Int) {
        this.id = id
        this.aisleId = aisleId
        this.storeId = storeId
        this.sortOrder = sortOrder
    }
}
