package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.Recipe;

public class RecipeDetailViewModel extends ViewModel {

    private LiveData<Recipe> recipe;

    RecipeDetailViewModel(AppDatabase database, long recipeId) {
        recipe = database.recipeDao().findById(recipeId);
    }

    public LiveData<Recipe> getRecipe() {
        return recipe;
    }
}
