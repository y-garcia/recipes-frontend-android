package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.custom.CustomRecipeIngredient;
import com.yeraygarcia.recipes.database.entity.custom.RecipeDetail;
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;

import java.util.List;

public class RecipeDetailViewModel extends ViewModel {

    private final RecipeDetailRepository mRecipeDetailRepository;
    private LiveData<RecipeDetail> mRecipeDetail;
    private LiveData<List<CustomRecipeIngredient>> mRecipeIngredients;

    RecipeDetailViewModel(RecipeDetailRepository repository, long recipeId) {
        mRecipeDetailRepository = repository;
        mRecipeDetail = mRecipeDetailRepository.getRecipeById(recipeId);
        mRecipeIngredients = mRecipeDetailRepository.getIngredientsByRecipeId(recipeId);
    }

    public LiveData<RecipeDetail> getRecipeDetail() {
        return mRecipeDetail;
    }

    public LiveData<List<CustomRecipeIngredient>> getRecipeIngredients() {
        return mRecipeIngredients;
    }

    public void update(Recipe recipe) {
        Debug.d(this, "update(recipe)");
        mRecipeDetailRepository.update(recipe);
    }
}
