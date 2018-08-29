package com.yeraygarcia.recipes.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.yeraygarcia.recipes.OnWebResponseListener;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RecipeDetailViewModel extends ViewModel {

    private final RecipeDetailRepository mRepository;
    private LiveData<Recipe> mRecipeDetail;
    private LiveData<List<UiRecipeIngredient>> mRecipeIngredients;
    private LiveData<List<RecipeStep>> mRecipeSteps;
    private LiveData<Boolean> mInShoppingList;
    private Recipe mRecipeDraft;
    private LiveData<RecipeStep> mRecipeStep;

    RecipeDetailViewModel(RecipeDetailRepository repository, UUID recipeId) {
        mRepository = repository;
        mRecipeDetail = mRepository.getRecipeById(recipeId);
        mRecipeIngredients = mRepository.getIngredientsByRecipeId(recipeId);
        mRecipeSteps = mRepository.getStepsByRecipeId(recipeId);
        mInShoppingList = mRepository.isInShoppingList(recipeId);
    }

    public LiveData<Recipe> getRecipe() {
        return mRecipeDetail;
    }

    public LiveData<List<UiRecipeIngredient>> getRecipeIngredients() {
        return mRecipeIngredients;
    }

    public LiveData<List<RecipeStep>> getRecipeSteps() {
        return mRecipeSteps;
    }

    public void update(Recipe recipe) {
        Debug.d(this, "update(recipe)");
        mRepository.update(recipe);
    }

    public LiveData<Boolean> isInShoppingList() {
        return mInShoppingList;
    }

    public void removeFromShoppingList(UUID recipeId) {
        mRepository.removeFromShoppingList(recipeId);
    }

    public void addToShoppingList(UUID recipeId) {
        mRepository.addToShoppingList(recipeId);
    }

    public void update(UiRecipeIngredient ingredient) {
        Debug.d(this, "update(ingredient)");
        mRepository.update(ingredient);
    }

    public void setRecipeDraft(Recipe recipeDraft) {
        mRecipeDraft = recipeDraft;
    }

    public Recipe getRecipeDraft(Recipe defaultValue) {
        return mRecipeDraft == null ? defaultValue : mRecipeDraft;
    }

    private void initDraft(Recipe defaultDraft) {
        if (mRecipeDraft == null) {
            mRecipeDraft = defaultDraft;
        }
    }

    public void saveDurationToDraft(@NotNull CharSequence duration, @Nullable Recipe defaultDraft) {
        initDraft(defaultDraft);

        if (mRecipeDraft != null) {
            Integer newDuration;
            try {
                newDuration = Integer.valueOf(duration.toString()) * 60;
            } catch (NumberFormatException e) {
                newDuration = null;
            }
            mRecipeDraft.setDuration(newDuration);
        }
    }

    public void saveSourceToDraft(@NotNull CharSequence source, @Nullable Recipe defaultDraft) {
        initDraft(defaultDraft);

        if (mRecipeDraft != null) {
            mRecipeDraft.setUrl(source.toString().trim());
        }
    }

    public void persistDraft() {
        Debug.d(this, "persistDraft()");
        if (mRecipeDraft != null) {
            mRepository.persistDraft(mRecipeDraft, this);
        }
    }

    public LiveData<RecipeStep> getRecipeStep(UUID id) {
        if (mRecipeStep == null || mRecipeStep.getValue() != null && mRecipeStep.getValue().getId() != id) {
            mRecipeStep = mRepository.getRecipeStep(id);
        }
        return mRecipeStep;
    }

    public void updateRecipeStep(RecipeStep recipeStep, OnWebResponseListener listener) {
        mRepository.updateRecipeStep(recipeStep, listener);
    }
}
