package com.yeraygarcia.recipes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.LongSparseArray;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.Unit;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.database.remote.Resource;
import com.yeraygarcia.recipes.database.repository.RecipeRepository;
import com.yeraygarcia.recipes.util.Debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRepository;

    private MutableLiveData<List<Long>> mTagFilter = new MutableLiveData<>();
    private LiveData<Resource<List<Recipe>>> mRecipes = Transformations.switchMap(mTagFilter, tagIds -> mRepository.getRecipesByTagId(tagIds));
    private LiveData<List<Tag>> mTags;
    private LiveData<List<Long>> mRecipeIdsInShoppingList;
    private LiveData<List<Recipe>> mRecipesInShoppingList;
    private final LiveData<List<UiShoppingListItem>> mShoppingListItems;
    private LiveData<List<Unit>> mUnits;

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
        mUnits = mRepository.getUnits();
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
        // TODO this is a hack :-(
        List<Long> tagFilterCopy = mTagFilter.getValue();
        clearTagFilter();
        mTagFilter.setValue(tagFilterCopy);
    }

    public void addItemToShoppingList(String newItem) {
        if (newItem != null && !newItem.trim().isEmpty()) {
            newItem = newItem.trim();

            String units = getUnitRegex();
            ShoppingListItem item;
            Double quantity;
            Long unit;
            String ingredient;

            Matcher matcher1 = Pattern.compile("([0-9]+(?:(?:.|,)[0-9]+)?) ?(" + units + ") (.+)$", Pattern.CASE_INSENSITIVE).matcher(newItem);
            Matcher matcher2 = Pattern.compile("([0-9]+(?:(?:.|,)[0-9]+)?) (.+)$", Pattern.CASE_INSENSITIVE).matcher(newItem);

            if (matcher1.matches()) {
                String q = matcher1.group(1).replace(",", ".");
                quantity = Double.valueOf(q);
                unit = getUnitIdByName(matcher1.group(2));
                ingredient = matcher1.group(3);

                item = new ShoppingListItem(ingredient, quantity, unit);

            } else if (matcher2.matches()) {
                String q = matcher2.group(1).replace(",", ".");
                quantity = Double.valueOf(q);
                ingredient = matcher2.group(2);

                item = new ShoppingListItem(ingredient, quantity, null);

            } else {
                item = new ShoppingListItem(newItem);
            }

            mRepository.addToShoppingList(item);
        }
    }

    private Long getUnitIdByName(String name) {
        List<Unit> units = mUnits.getValue();

        if (units == null) {
            return null;
        }

        for (Unit unit : units) {
            if (unit.getNameSingular().equals(name) || unit.getNamePlural().equals(name)) {
                return unit.getId();
            }
        }

        return null;
    }

    private String getUnitRegex() {
        List<Unit> units = mUnits.getValue();

        if (units == null) {
            return "[a-zäöü]+";
        }

        StringBuilder regex = new StringBuilder();

        for (Unit unit : units) {
            regex.append(unit.getNameSingular()).append("|");

            if (!unit.getNameSingular().equals(unit.getNamePlural())) {
                regex.append(unit.getNamePlural()).append("|");
            }
        }

        regex.deleteCharAt(regex.length() - 1);

        return regex.toString();
    }

    public void clearCompletedFromShoppingList() {
        mRepository.clearCompletedFromShoppingList();
    }

    public LiveData<List<Unit>> getUnits() {
        return mUnits;
    }
}
