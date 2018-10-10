package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import com.yeraygarcia.recipes.R
import java.util.*
import java.util.concurrent.TimeUnit

@Entity(tableName = "recipe")
data class Recipe(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID,
    var name: String,
    var portions: Int = 0,
    var duration: Int?,
    var url: String?
) {
    val durationInMinutes: Long
        get() = TimeUnit.SECONDS.toMinutes(duration?.toLong() ?: 0)

    override fun toString(): String {
        return "\n[Recipe: $name, Portions: $portions, Duration: $durationInMinutes minutes, Source: $url]"
    }

    fun increasePortions() {
        portions += 1
    }

    fun decreasePortions() {
        portions -= 1
    }

    fun getFormattedDuration(context: Context): String {
        return context.getString(
            R.string.duration_format,
            String.format(Locale.getDefault(), "%d", durationInMinutes)
        )
    }
}
