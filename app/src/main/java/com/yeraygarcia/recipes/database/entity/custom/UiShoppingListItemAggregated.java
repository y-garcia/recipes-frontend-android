package com.yeraygarcia.recipes.database.entity.custom;

import android.support.annotation.NonNull;

public class UiShoppingListItemAggregated extends UiRecipeIngredient {

    @NonNull
    private Boolean completed;

    private String aisle;

    public UiShoppingListItemAggregated(@NonNull Boolean completed, String aisle) {
        this.completed = completed;
        this.aisle = aisle;
    }

    @NonNull
    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(@NonNull Boolean completed) {
        this.completed = completed;
    }

    public String getAisle() {
        return aisle;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }
}
