package com.yeraygarcia.recipes.database.entity.custom

import android.arch.persistence.room.ColumnInfo
import java.util.*
import kotlin.math.round

data class UiRecipeIngredient(

    var id: UUID,

    var portions: Int,

    var quantity: Double?,

    @ColumnInfo(name = "unit_name")
    var unitName: String?,

    @ColumnInfo(name = "unit_name_plural")
    var unitNamePlural: String?,

    var name: String
) {

    val formattedQuantity: String
        get() = when {
            quantity == null -> ""
            quantity!! * portions == round(quantity!! * portions) -> String.format(
                Locale.getDefault(),
                "%d",
                java.lang.Double.valueOf(quantity!! * portions)!!.toInt()
            )
            else -> String.format(Locale.getDefault(), "%1$,.2f", quantity!! * portions)
        }

    val formattedUnit: String?
        get() = if (quantity != null && quantity!! * portions == 1.0) {
            unitName
        } else {
            unitNamePlural
        }

    val formattedQuantityAndUnit: String
        get() {
            val quantityAndUnit = StringBuilder()

            if (quantity != null) {
                quantityAndUnit.append(formattedQuantity)
            }
            if (quantity != null && unitName != null && unitNamePlural != null) {
                quantityAndUnit.append(" ").append(formattedUnit)
            }

            return quantityAndUnit.toString()
        }

    override fun toString(): String {
        val ingredient = StringBuilder()

        if (quantity != null) {
            ingredient.append(formattedQuantity).append(" ")
        }
        if (quantity != null && unitName != null && unitNamePlural != null) {
            ingredient.append(formattedUnit).append(" ")
        }

        return ingredient.append(name).toString()
    }
}
