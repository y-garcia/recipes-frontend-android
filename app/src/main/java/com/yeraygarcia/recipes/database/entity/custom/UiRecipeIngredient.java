package com.yeraygarcia.recipes.database.entity.custom;

import android.arch.persistence.room.ColumnInfo;

public class UiRecipeIngredient {

    private Double quantity;

    @ColumnInfo(name = "unit_name")
    private String unitName;

    @ColumnInfo(name = "unit_name_plural")
    private String unitNamePlural;

    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitNamePlural() {
        return unitNamePlural;
    }

    public void setUnitNamePlural(String unitNamePlural) {
        this.unitNamePlural = unitNamePlural;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    @Override
    public String toString() {
        StringBuilder ingredient = new StringBuilder();

        if (quantity == null) {
            return ingredient.append(ingredientName).toString();
        }

        if(quantity == Math.rint(quantity)){
            ingredient.append(quantity.intValue()).append(" ");
        }
        else {
            ingredient.append(quantity).append(" ");
        }

        if (unitName != null) {
            if (quantity == 1) {
                ingredient.append(unitName).append(" ");
            } else {
                ingredient.append(unitNamePlural).append(" ");
            }
        }

        return ingredient.append(ingredientName).toString();
    }
}
