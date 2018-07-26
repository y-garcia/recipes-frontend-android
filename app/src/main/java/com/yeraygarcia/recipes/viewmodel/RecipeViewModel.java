package com.yeraygarcia.recipes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.LongSparseArray;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.database.remote.Resource;
import com.yeraygarcia.recipes.database.repository.RecipeRepository;
import com.yeraygarcia.recipes.util.Debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRepository;

    private MutableLiveData<List<Long>> mTagFilter = new MutableLiveData<>();
    private LiveData<Resource<List<Recipe>>> mRecipes = Transformations.switchMap(mTagFilter, tagIds -> mRepository.getRecipesByTagId(tagIds));
    private LiveData<List<Tag>> mTags;
    private LiveData<List<Long>> mRecipeIdsInShoppingList;
    private LiveData<List<Recipe>> mRecipesInShoppingList;
    private final LiveData<List<UiShoppingListItem>> mShoppingListItems;

    private MutableLiveData<LongSparseArray<UiShoppingListItem>> mShoppingListItemsDraft = new MutableLiveData<>();

    public RecipeViewModel(Application application) {
        super(application);
        Debug.d(this, "RecipeViewModel(application)");

        mRepository = new RecipeRepository(application);

        mTags = mRepository.getTags();
        mTagFilter.setValue(new ArrayList<>());

        mRecipeIdsInShoppingList = mRepository.getRecipeIdsInShoppingList();
        mRecipesInShoppingList = mRepository.getRecipesInShoppingList();
        mShoppingListItems = mRepository.getShoppingListItems();
        mShoppingListItemsDraft.setValue(new LongSparseArray<>());
    }

    public LiveData<Resource<List<Recipe>>> getRecipes() {
        Debug.d(this, "getRecipes()");
        return mRecipes;
    }

    public LiveData<List<Tag>> getTags() {
        Debug.d(this, "getTags()");
        return mTags;
    }

    public MutableLiveData<List<Long>> getTagFilter() {
        Debug.d(this, "getTagFilter()");
        return mTagFilter;
    }

    public long[] getTagFilterAsArray() {
        Debug.d(this, "getTagFilterAsArray()");
        List<Long> tagFilter = mTagFilter.getValue();
        if (tagFilter == null) {
            return new long[]{};
        }
        long[] result = new long[tagFilter.size()];

        for (int i = 0; i < tagFilter.size(); i++) {
            result[i] = tagFilter.get(i);
        }
        return result;
    }

    public void addTagToFilter(Tag tag) {
        Debug.d(this, "addTagToFilter(" + tag.getId() + ")");
        List<Long> newTags = mTagFilter.getValue();
        if (newTags == null) {
            newTags = new ArrayList<>();
        }
        newTags.add(tag.getId());
        mTagFilter.setValue(newTags);
        mRepository.logTagUsage(tag.getId());
    }

    public void removeTagFromFilter(Tag tag) {
        Debug.d(this, "removeTagFromFilter(" + tag.getId() + ")");
        List<Long> newTags = mTagFilter.getValue();
        if (newTags == null) {
            return;
        }
        newTags.remove(tag.getId());
        mTagFilter.setValue(newTags);
    }

    public void setTagFilter(long[] tagFilter) {
        Debug.d(this, "setTagFilter(tagFilter)");
        List<Long> tagFilterList = new ArrayList<>();
        for (long tagId : tagFilter) {
            tagFilterList.add(tagId);
        }
        mTagFilter.setValue(tagFilterList);
    }

    public void updateTagUsage() {
        Debug.d(this, "updateTagUsage()");
        mRepository.updateTagUsage();
    }

    public void addRecipeToShoppingList(Recipe recipe) {
        mRepository.addToShoppingList(recipe);
    }

    public void updatePortionsInShoppingList(Recipe recipe) {
        mRepository.update(recipe);
        mRepository.updatePortionsInShoppingList(recipe);
    }

    public void updatePortionsInShoppingList(UiShoppingListItem shoppingListItem) {
        mRepository.updatePortionsInShoppingList(shoppingListItem);
    }

    public LiveData<List<Recipe>> getRecipesInShoppingList() {
        return mRecipesInShoppingList;
    }

    public LiveData<List<Long>> getRecipeIdsInShoppingList() {
        return mRecipeIdsInShoppingList;
    }

    public void deleteRecipeFromShoppingList(Recipe recipe) {
        mRepository.removeFromShoppingList(recipe);
    }

    public void markComplete(UiShoppingListItem shoppingListItem, boolean complete) {
        shoppingListItem.setCompleted(complete);
        mRepository.update(shoppingListItem);
    }

    public LiveData<List<UiShoppingListItem>> getShoppingListItems() {
        return mShoppingListItems;
    }

    public void removeFromShoppingList(UiShoppingListItem shoppingListItem) {
        mRepository.removeFromShoppingList(shoppingListItem);
    }

    public void saveDraft(UiShoppingListItem ingredient) {
        Debug.d(this, "saveDraft(ingredient = " + ingredient.toString() + ")");
        LongSparseArray<UiShoppingListItem> ingredientsDraft = mShoppingListItemsDraft.getValue();
        if (ingredientsDraft == null) {
            ingredientsDraft = new LongSparseArray<>();
        }
        ingredientsDraft.put(ingredient.getId(), ingredient);
        mShoppingListItemsDraft.setValue(ingredientsDraft);
        Debug.d(this, "mShoppingListItemsDraft = " + mShoppingListItemsDraft.getValue() + ")");
    }

    public void persistDraft() {
        Debug.d(this, "persistDraft()");
        Debug.d(this, "mShoppingListItemsDraft = " + mShoppingListItemsDraft + ")");

        LongSparseArray<UiShoppingListItem> shoppingListItemsDraft = mShoppingListItemsDraft.getValue();
        if (shoppingListItemsDraft == null) {
            shoppingListItemsDraft = new LongSparseArray<>();
        }
        UiShoppingListItem[] shoppingListItems = new UiShoppingListItem[shoppingListItemsDraft.size()];
        for (int i = 0; i < shoppingListItemsDraft.size(); i++) {
            shoppingListItems[i] = shoppingListItemsDraft.valueAt(i);
        }
        mRepository.update(shoppingListItems);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

    public void refreshAll() {
        mRepository.invalidateCache();
        forceRefresh();
    }

    private void clearTagFilter() {
        mTagFilter.setValue(Collections.emptyList());
    }

    private void forceRefresh() {
        List<Long> tagFilterCopy = mTagFilter.getValue();
        clearTagFilter();
        mTagFilter.setValue(tagFilterCopy);
    }
}
