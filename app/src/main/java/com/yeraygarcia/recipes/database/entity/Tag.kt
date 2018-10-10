package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "tag", indices = [Index(value = ["name"], unique = true)])
data class Tag(
    @PrimaryKey
    var id: UUID,

    var name: String,

    @ColumnInfo(name = "usage_count")
    @SerializedName("usage_count")
    var usageCount: Long,

    @ColumnInfo(name = "last_used")
    @SerializedName("last_used")
    var lastUsed: Long = System.currentTimeMillis() / 1000L
)
