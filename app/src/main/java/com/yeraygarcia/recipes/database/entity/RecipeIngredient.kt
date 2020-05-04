package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.ForeignKey.RESTRICT
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "recipe_ingredient",
    indices = [Index("recipe_id"), Index("ingredient_id"), Index("unit_id")],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("recipe_id"),
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("ingredient_id"),
            onDelete = RESTRICT
        ),
        ForeignKey(
            entity = Unit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("unit_id"),
            onDelete = RESTRICT
        )
    ]
)
data class RecipeIngredient(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID,

    @ColumnInfo(name = "recipe_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("recipe_id")
    var recipeId: UUID,

    @ColumnInfo(name = "ingredient_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("ingredient_id")
    var ingredientId: UUID,

    var quantity: Double?,

    @ColumnInfo(name = "unit_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("unit_id")
    var unitId: UUID?,

    @ColumnInfo(name = "sort_order")
    @SerializedName("sort_order")
    var sortOrder: Int = 0,

    var created: Long = System.currentTimeMillis(),

    var modified: Long = System.currentTimeMillis()
)
