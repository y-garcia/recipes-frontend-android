package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.yeraygarcia.recipes.database.UUIDTypeConverter
import java.util.*

@Entity(
    tableName = "usage_tag",
    indices = [Index("tag_id")],
    foreignKeys = [ForeignKey(
        entity = Tag::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("tag_id"),
        onDelete = CASCADE
    )]
)
data class TagUsage(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID = UUIDTypeConverter.newUUID(),

    var created: Long = System.currentTimeMillis() / 1000L,

    @ColumnInfo(name = "tag_id", typeAffinity = ColumnInfo.BLOB)
    var tagId: UUID
)
