package com.yeraygarcia.recipes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.database.repository.RecipeRepository;
import com.yeraygarcia.recipes.util.Debug;

import java.util.List;

public class ShoppingListViewModel extends AndroidViewModel {

    private RecipeRepository mRepository;

    private final LiveData<List<UiShoppingListItem>> mShoppingListItems;


    public ShoppingListViewModel(Application application) {
        super(application);
        Debug.d(this, "ShoppingListViewModel(application)");
        mRepository = new RecipeRepository(application);
        //mRecipes = mRepository.getRecipes();
        mShoppingListItems = mRepository.getShoppingListItems();
    }

    public LiveData<List<UiShoppingListItem>> getShoppingListItems() {
        return mShoppingListItems;
    }

    public void removeFromShoppingList(UiShoppingListItem shoppingListItem) {
        mRepository.removeFromShoppingList(shoppingListItem);
    }
}
