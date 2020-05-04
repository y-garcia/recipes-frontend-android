package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "deleted", indices = [Index("tableId"), Index("tableName")])
data class Deleted(
    @PrimaryKey
    var id: Int,
    var tableId: Int,
    var tableName: String,
    var deletedId: UUID,
    var deleted: Long = System.currentTimeMillis()
)
