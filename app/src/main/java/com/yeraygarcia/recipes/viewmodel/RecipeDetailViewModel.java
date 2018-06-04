package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;

import java.util.List;

public class RecipeDetailViewModel extends ViewModel {

    private LiveData<Recipe> recipe;
    private LiveData<List<RecipeIngredient>> recipeIngredients;
    private LiveData<List<RecipeStep>> recipeSteps;

    RecipeDetailViewModel(AppDatabase database, long recipeId) {
        recipe = database.recipeDao().findById(recipeId);
        recipeIngredients = database.recipeIngredientDao().findByRecipeId(recipeId);
        recipeSteps = database.recipeStepDao().findByRecipeId(recipeId);
    }

    public LiveData<Recipe> getRecipe() {
        return recipe;
    }

    public LiveData<List<RecipeIngredient>> getRecipeIngredients() {
        return recipeIngredients;
    }

    public LiveData<List<RecipeStep>> getRecipeSteps() {
        return recipeSteps;
    }
}
