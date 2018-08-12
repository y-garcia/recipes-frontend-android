package com.yeraygarcia.recipes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.LongSparseArray;

import com.yeraygarcia.recipes.database.entity.Ingredient;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.Unit;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
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
    private LiveData<UiShoppingListItem> mShoppingListItem;
    private LiveData<List<String>> mUnitsAndIngredientNames;
    private LiveData<List<String>> mIngredientNames;
    private LiveData<List<String>> mUnitNames;
    private LiveData<UiRecipeIngredient> mRecipeIngredient;
    private LiveData<List<Ingredient>> mIngredients;

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
        mUnitsAndIngredientNames = mRepository.getUnitsAndIngredientNames();
        mIngredientNames = mRepository.getIngredientNames();
        mUnitNames = mRepository.getUnitNames();
        mIngredients = mRepository.getIngredients();
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

    public void setTagFilter(Tag tag) {
        Debug.d(this, "setTagFilter(tag)");
        List<Long> tagFilterList = new ArrayList<>();
        tagFilterList.add(tag.getId());
        mTagFilter.setValue(tagFilterList);
        mRepository.logTagUsage(tag.getId());
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

    public Long getUnitIdByName(String name) {
        Debug.d(this, "getUnitIdByName(" + (name == null ? "null" : name) + ")");
        List<Unit> units = mUnits.getValue();

        if (units == null || name == null || name.isEmpty()) {
            return null;
        }

        name = name.toLowerCase();

        for (Unit unit : units) {
            String debugMessage = name + " = " + unit.getNameSingular().toLowerCase() + " or " + unit.getNamePlural().toLowerCase() + " ? ";
            if (unit.getNameSingular().toLowerCase().equals(name) || unit.getNamePlural().toLowerCase().equals(name)) {
                Debug.d(this, debugMessage + "true");
                return unit.getId();
            }
            Debug.d(this, debugMessage + "false");
        }

        return null;
    }

    public Long getIngredientIdByName(String name) {
        Debug.d(this, "getIngredientIdByName(" + (name == null ? "null" : name) + ")");
        List<Ingredient> ingredients = mIngredients.getValue();

        if (ingredients == null || name == null || name.isEmpty()) {
            return null;
        }

        name = name.toLowerCase();

        for (Ingredient ingredient : ingredients) {
            String debugMessage = name + " = " + ingredient.getName().toLowerCase() + " ? ";
            if (ingredient.getName().toLowerCase().equals(name)) {
                Debug.d(this, debugMessage + "true");
                return ingredient.getId();
            }
            Debug.d(this, debugMessage + "false");
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

    public LiveData<UiShoppingListItem> getShoppingListItem(long id) {
        if (mShoppingListItem == null || mShoppingListItem.getValue() != null && mShoppingListItem.getValue().getId() != id) {
            mShoppingListItem = mRepository.getShoppingListItemById(id);
        }
        return mShoppingListItem;
    }

    public void updateShoppingListItem(Long id, String ingredient, Double quantity, Long unitId) {
        mRepository.updateShoppingListItem(id, ingredient, quantity, unitId);
    }

    public LiveData<List<String>> getUnitsAndIngredientNames() {
        return mUnitsAndIngredientNames;
    }

    public LiveData<List<String>> getIngredientNames() {
        return mIngredientNames;
    }

    public LiveData<List<String>> getUnitNames() {
        return mUnitNames;
    }

    public LiveData<UiRecipeIngredient> getIngredient(long id) {
        if (mRecipeIngredient == null || mRecipeIngredient.getValue() != null && mRecipeIngredient.getValue().getId() != id) {
            mRecipeIngredient = mRepository.getUiRecipeIngredient(id);
        }
        return mRecipeIngredient;
    }

    public void updateRecipeIngredient(long id, String ingredientName, Double quantity, Long unitId) {
        mRepository.updateRecipeIngredient(id, ingredientName, quantity, unitId);
    }
}
