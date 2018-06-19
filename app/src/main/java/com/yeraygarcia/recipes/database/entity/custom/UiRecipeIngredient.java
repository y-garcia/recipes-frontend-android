package com.yeraygarcia.recipes.database.entity.custom;

import android.arch.persistence.room.ColumnInfo;

import java.util.Locale;

public class UiRecipeIngredient {

    private Long id;

    private Integer portions;

    private Double quantity;

    @ColumnInfo(name = "unit_name")
    private String unitName;

    @ColumnInfo(name = "unit_name_plural")
    private String unitNamePlural;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormattedQuantity() {
        if (quantity == null) {
            return "";
        } else if (quantity * portions == Math.rint(quantity * portions)) {
            // quantity is an integer, use 0 format
            return String.format(Locale.getDefault(), "%d", Double.valueOf(quantity * portions).intValue());
        } else {
            // quantity is a double, use 0.00 format
            return String.format(Locale.getDefault(), "%1$,.2f", quantity * portions);
        }
    }

    public String getFormattedUnit() {
        if (quantity != null && quantity == 1) {
            return unitName;
        } else {
            return unitNamePlural;
        }
    }

    @Override
    public String toString() {
        StringBuilder ingredient = new StringBuilder();

        if (quantity == null) {
            return ingredient.append(name).toString();
        }

        if (quantity == Math.rint(quantity)) {
            ingredient.append(quantity.intValue()).append(" ");
        } else {
            ingredient.append(quantity).append(" ");
        }

        if (unitName != null) {
            if (quantity == 1) {
                ingredient.append(unitName).append(" ");
            } else {
                ingredient.append(unitNamePlural).append(" ");
            }
        }

        return ingredient.append(name).toString();
    }
}
