package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.ForeignKey.RESTRICT
import com.yeraygarcia.recipes.database.UUIDTypeConverter
import java.util.*

@Entity(
    tableName = "shopping_list_item",
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
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Unit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("unit_id"),
            onDelete = RESTRICT
        )
    ]
)
data class ShoppingListItem(

    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID = UUIDTypeConverter.newUUID(),

    @ColumnInfo(name = "recipe_id", typeAffinity = ColumnInfo.BLOB)
    var recipeId: UUID? = null,

    @ColumnInfo(name = "ingredient_id", typeAffinity = ColumnInfo.BLOB)
    var ingredientId: UUID? = null,

    var name: String,

    var quantity: Double? = null,

    @ColumnInfo(name = "unit_id", typeAffinity = ColumnInfo.BLOB)
    var unitId: UUID? = null,

    @ColumnInfo(name = "sort_order")
    var sortOrder: Long = 0,

    var completed: Boolean = false,

    var visible: Boolean = true
)
