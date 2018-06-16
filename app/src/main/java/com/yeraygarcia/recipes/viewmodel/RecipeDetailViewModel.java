package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipe;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;

import java.util.List;

public class RecipeDetailViewModel extends ViewModel {

    private final RecipeDetailRepository mRecipeDetailRepository;
    private LiveData<UiRecipe> mRecipeDetail;
    private LiveData<List<UiRecipeIngredient>> mRecipeIngredients;
    private LiveData<Boolean> mInShoppingList;

    RecipeDetailViewModel(RecipeDetailRepository repository, long recipeId) {
        mRecipeDetailRepository = repository;
        mRecipeDetail = mRecipeDetailRepository.getRecipeById(recipeId);
        mRecipeIngredients = mRecipeDetailRepository.getIngredientsByRecipeId(recipeId);
        mInShoppingList = mRecipeDetailRepository.isInShoppingList(recipeId);
    }

    public LiveData<UiRecipe> getRecipeDetail() {
        return mRecipeDetail;
    }

    public LiveData<List<UiRecipeIngredient>> getRecipeIngredients() {
        return mRecipeIngredients;
    }

    public void update(Recipe recipe) {
        Debug.d(this, "update(recipe)");
        mRecipeDetailRepository.update(recipe);
    }

    public LiveData<Boolean> isInShoppingList() {
        return mInShoppingList;
    }

    public void removeFromShoppingList(long recipeId) {
        mRecipeDetailRepository.removeFromShoppingList(recipeId);
    }

    public void addToShoppingList(long recipeId) {
        mRecipeDetailRepository.addToShoppingList(recipeId);
    }
}
