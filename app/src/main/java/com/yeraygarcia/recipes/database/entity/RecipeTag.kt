package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "recipe_tag",
    indices = [Index(
        value = arrayOf("recipe_id", "tag_id"),
        unique = true
    ), Index("recipe_id"), Index("tag_id")],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("recipe_id"),
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("tag_id"),
            onDelete = CASCADE
        )
    ]
)
data class RecipeTag(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID,

    @ColumnInfo(name = "recipe_id")
    @SerializedName("recipe_id")
    var recipeId: UUID,

    @ColumnInfo(name = "tag_id")
    @SerializedName("tag_id")
    var tagId: UUID
)
