package com.yeraygarcia.recipes.database.entity.custom;

import android.arch.persistence.room.ColumnInfo;

import java.util.Locale;
import java.util.UUID;

public class UiRecipeIngredient {

    private UUID id;

    private Integer portions;

    private Double quantity;

    @ColumnInfo(name = "unit_name")
    private String unitName;

    @ColumnInfo(name = "unit_name_plural")
    private String unitNamePlural;

    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

        if (quantity != null) {
            ingredient.append(getFormattedQuantity()).append(" ");
        }
        if (quantity != null && unitName != null && unitNamePlural != null) {
            ingredient.append(getFormattedUnit()).append(" ");
        }

        return ingredient.append(name).toString();
    }
}
