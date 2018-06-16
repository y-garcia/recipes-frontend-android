package com.yeraygarcia.recipes.database.entity.custom;

import android.support.annotation.NonNull;

public class UiShoppingListItem extends UiShoppingListItemAggregated {

    @NonNull
    private Long id;

    public UiShoppingListItem(@NonNull Long id, @NonNull Boolean completed, String aisle) {
        super(completed, aisle);
        this.id = id;
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }
}
