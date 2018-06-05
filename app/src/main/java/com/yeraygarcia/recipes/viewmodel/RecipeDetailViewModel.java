package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.custom.CustomRecipeIngredient;
import com.yeraygarcia.recipes.database.entity.custom.RecipeDetail;

import java.util.List;

public class RecipeDetailViewModel extends ViewModel {

    private LiveData<RecipeDetail> recipeDetail;
    private LiveData<List<CustomRecipeIngredient>> recipeIngredients;

    RecipeDetailViewModel(AppDatabase database, long recipeId) {
        recipeDetail = database.recipeDetailDao().findById(recipeId);
        recipeIngredients = database.recipeIngredientDao().findByRecipeId(recipeId);
    }

    public LiveData<RecipeDetail> getRecipeDetail() {
        return recipeDetail;
    }

    public LiveData<List<CustomRecipeIngredient>> getRecipeIngredients() {
        return recipeIngredients;
    }
}
