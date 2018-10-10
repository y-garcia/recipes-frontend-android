package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "recipe_step",
    indices = [Index("recipe_id")],
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("recipe_id"),
        onDelete = CASCADE
    )]
)
data class RecipeStep(

    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID,

    @ColumnInfo(name = "recipe_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("recipe_id")
    var recipeId: UUID,

    var description: String,

    @ColumnInfo(name = "is_section")
    @SerializedName("is_section")
    var isSection: Boolean = false,

    @ColumnInfo(name = "sort_order")
    @SerializedName("sort_order")
    var sortOrder: Int = 0
) {
    override fun toString(): String {
        return "$sortOrder. $description"
    }
}
