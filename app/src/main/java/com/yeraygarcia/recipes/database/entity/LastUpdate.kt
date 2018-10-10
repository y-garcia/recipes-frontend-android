package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "last_update")
data class LastUpdate(@field:ColumnInfo(name = "last_update") var lastUpdate: Long) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 1
}
