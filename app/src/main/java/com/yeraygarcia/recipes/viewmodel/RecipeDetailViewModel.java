package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.LongSparseArray;

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
    private LiveData<List<String>> mUnitNames;

    private MutableLiveData<LongSparseArray<UiRecipeIngredient>> mRecipeIngredientsDraft = new MutableLiveData<>();

    RecipeDetailViewModel(RecipeDetailRepository repository, long recipeId) {
        mRecipeDetailRepository = repository;
        mRecipeDetail = mRecipeDetailRepository.getRecipeById(recipeId);
        mRecipeIngredients = mRecipeDetailRepository.getIngredientsByRecipeId(recipeId);
        mInShoppingList = mRecipeDetailRepository.isInShoppingList(recipeId);
        mUnitNames = mRecipeDetailRepository.getUnitNames();
        mRecipeIngredientsDraft.setValue(new LongSparseArray<>());
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

    public LiveData<List<String>> getUnitNames() {
        return mUnitNames;
    }

    public void update(UiRecipeIngredient ingredient) {
        Debug.d(this, "update(ingredient)");
        mRecipeDetailRepository.update(ingredient);
    }

    public void saveDraft(UiRecipeIngredient ingredient) {
        Debug.d(this, "saveDraft(ingredient = " + ingredient.toString() + ")");
        LongSparseArray<UiRecipeIngredient> ingredientsDraft = mRecipeIngredientsDraft.getValue();
        if (ingredientsDraft == null) {
            ingredientsDraft = new LongSparseArray<>();
        }
        ingredientsDraft.put(ingredient.getId(), ingredient);
        mRecipeIngredientsDraft.setValue(ingredientsDraft);
        Debug.d(this, "mRecipeIngredientsDraft = " + mRecipeIngredientsDraft.getValue() + ")");
    }

    public void persistDraft() {
        Debug.d(this, "persistDraft()");
        Debug.d(this, "mRecipeIngredientsDraft = " + mRecipeIngredientsDraft + ")");

        LongSparseArray<UiRecipeIngredient> ingredientsDraft = mRecipeIngredientsDraft.getValue();
        if (ingredientsDraft == null) {
            ingredientsDraft = new LongSparseArray<>();
        }
        UiRecipeIngredient[] ingredients = new UiRecipeIngredient[ingredientsDraft.size()];
        for (int i = 0; i < ingredientsDraft.size(); i++) {
            ingredients[i] = ingredientsDraft.valueAt(i);
        }
        mRecipeDetailRepository.update(ingredients);
    }

    public LongSparseArray<UiRecipeIngredient> getRecipeIngredientsDraft() {
        return mRecipeIngredientsDraft.getValue();
    }
}
