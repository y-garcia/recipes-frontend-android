package com.yeraygarcia.recipes.database.entity.custom

import android.arch.persistence.room.ColumnInfo
import java.util.*
import kotlin.math.round

data class UiShoppingListItem(
    var id: UUID,
    var quantity: Double?,
    @ColumnInfo(name = "unit_name") var unitName: String?,
    @ColumnInfo(name = "unit_name_plural") var unitNamePlural: String?,
    var name: String,
    var completed: Boolean = false,
    var aisle: String? = null,
    var recipe: String? = null
) {
    val formattedQuantity: String
        get() = when (quantity) {
            null -> ""
            round(quantity as Double) -> String.format(Locale.getDefault(), "%d", quantity?.toInt())
            else -> String.format(Locale.getDefault(), "%1$,.2f", quantity)
        }

    val formattedUnit: String
        get() = (if (quantity == 1.0) unitName else unitNamePlural) ?: ""

    override fun toString(): String {

        if (quantity == null) {
            return name
        }

        var ingredient =
            if (quantity == round(quantity!!)) "${quantity!!.toInt()} " else "$quantity "
        if (unitName != null) {
            ingredient += if (quantity == 1.0) "$unitName " else "$unitNamePlural "
        }
        ingredient += name

        return ingredient
    }
}
