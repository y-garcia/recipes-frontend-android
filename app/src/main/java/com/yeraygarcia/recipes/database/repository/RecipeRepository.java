package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yeraygarcia.recipes.AppExecutors;
import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.LastUpdate;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.TagUsage;
import com.yeraygarcia.recipes.database.entity.Unit;
import com.yeraygarcia.recipes.database.entity.custom.All;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.database.remote.ApiResponse;
import com.yeraygarcia.recipes.database.remote.NetworkBoundResource;
import com.yeraygarcia.recipes.database.remote.Resource;
import com.yeraygarcia.recipes.database.remote.ResourceData;
import com.yeraygarcia.recipes.database.remote.RetrofitInstance;
import com.yeraygarcia.recipes.database.remote.Webservice;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class RecipeRepository {

    private Webservice mWebservice;

    private final AppExecutors mAppExecutors;

    private Context mContext;
    private AppDatabase mDb;

    private LiveData<List<UiShoppingListItem>> mShoppingListItems;
    private LiveData<List<Long>> mRecipeIdsInShoppingList;
    private LiveData<List<Recipe>> mRecipesInShoppingList;

    private boolean mCacheIsDirty = true;

    public RecipeRepository(Application application) {
        mContext = application;
        mDb = AppDatabase.getDatabase(application);

        mWebservice = RetrofitInstance.get().create(Webservice.class);
        mAppExecutors = new AppExecutors();

        mShoppingListItems = mDb.getShoppingListDao().findAll();
        mRecipeIdsInShoppingList = mDb.getShoppingListDao().findDistinctRecipeIds();
        mRecipesInShoppingList = mDb.getRecipeDao().findRecipesInShoppingList();
    }

    // Methods

    private boolean canFetch() {
        return NetworkUtil.isOnline(mContext) && RetrofitInstance.get().getIdToken() != null;
    }

    private void updateAllFromResource(ResourceData<All> resource) {
        All data = resource.getResult();

        mDb.getRecipeTagDao().deleteIfNotIn(data.getRecipeTags());
        mDb.getRecipeStepDao().deleteIfNotIn(data.getRecipeSteps());
        mDb.getRecipeIngredientDao().deleteIfNotIn(data.getRecipeIngredients());
        mDb.getRecipeDao().deleteIfNotIn(data.getRecipes());
        mDb.getIngredientDao().deleteIfNotIn(data.getIngredients());
        mDb.getTagDao().deleteIfNotIn(data.getTags());
        mDb.getUnitDao().deleteIfNotIn(data.getUnits());
        mDb.getAisleDao().deleteIfNotIn(data.getAisles());

        mDb.getAisleDao().upsert(data.getAisles());
        mDb.getUnitDao().upsert(data.getUnits());
        mDb.getTagDao().upsert(data.getTags());
        mDb.getIngredientDao().upsert(data.getIngredients());
        mDb.getRecipeDao().upsert(data.getRecipes());
        mDb.getRecipeIngredientDao().upsert(data.getRecipeIngredients());
        mDb.getRecipeStepDao().upsert(data.getRecipeSteps());
        mDb.getRecipeTagDao().upsert(data.getRecipeTags());

        updateTagUsage();

        mDb.getLastUpdateDao().upsert(new LastUpdate(System.currentTimeMillis()));
        mCacheIsDirty = false;
    }

    public void invalidateCache() {
        mCacheIsDirty = true;
    }

    public LiveData<Resource<List<Recipe>>> getRecipes() {
        return new NetworkBoundResource<List<Recipe>, ResourceData<All>>(mAppExecutors) {
            @Override
            protected void saveCallResult(@NonNull ResourceData<All> callResult) {
                Debug.d(this, "getRecipes().saveCallResult(callResult)");
                updateAllFromResource(callResult);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Recipe> data) {
                Debug.d(this, "getRecipes().shouldFetch(" + (data == null ? "null" : "[" + data.size() + "]") + ")");
                return (data == null || data.isEmpty() || mCacheIsDirty) && canFetch();
            }

            @NonNull
            @Override
            protected LiveData<List<Recipe>> loadFromDb() {
                Debug.d(this, "getRecipes().loadFromDb()");
                return mDb.getRecipeDao().findAll();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResourceData<All>>> createCall() {
                Debug.d(this, "getRecipes().createCall()");
                return mWebservice.getAll();
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Recipe>>> getRecipesByTagId(List<Long> tagIds) {
        Debug.d(this, "getRecipesByTagId(" + tagIds + ")");

        if (tagIds.size() == 0) {
            return getRecipes();
        }

        return new NetworkBoundResource<List<Recipe>, ResourceData<All>>(mAppExecutors) {
            @Override
            protected void saveCallResult(@NonNull ResourceData<All> callResult) {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ").saveCallResult(callResult)");
                updateAllFromResource(callResult);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Recipe> data) {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ").shouldFetch(" + (data == null ? "null" : "[" + data.size() + "]") + ")");
                return mCacheIsDirty && canFetch();
            }

            @NonNull
            @Override
            protected LiveData<List<Recipe>> loadFromDb() {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ").loadFromDb()");
                return mDb.getRecipeTagDao().findRecipesByAllTagIds(tagIds, tagIds.size());
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResourceData<All>>> createCall() {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ").createCall()");
                return mWebservice.getAll();
            }
        }.asLiveData();
    }

    public LiveData<List<Tag>> getTags() {
        Debug.d(this, "getTags()");
        return mDb.getTagDao().findAll();
    }

    public void insert(Recipe recipe) {
        mAppExecutors.diskIO().execute(() -> mDb.getRecipeDao().insert(recipe));
    }

    public void update(Recipe recipe) {
        mAppExecutors.diskIO().execute(() -> mDb.getRecipeDao().update(recipe));
    }

    public void logTagUsage(Long tagId) {
        TagUsage tagUsage = new TagUsage(tagId);
        mAppExecutors.diskIO().execute(() -> mDb.getTagUsageDao().insert(tagUsage));
    }

    public void updateTagUsage() {
        mAppExecutors.diskIO().execute(() -> {
            List<Tag> tags = mDb.getTagUsageDao().getTagsWithUpdatedUsage();
            mDb.getTagDao().update(tags);
        });
    }

    public void addToShoppingList(Recipe recipe) {
        Debug.d(this, "Adding '" + recipe.getName() + "' to shopping list");
        mAppExecutors.diskIO().execute(() -> {
            final long recipeId = recipe.getId();
            List<ShoppingListItem> shoppingListItems = mDb.getRecipeIngredientDao().findShoppingListItemByRecipeId(recipeId);
            mDb.getShoppingListDao().deleteByRecipeId(recipeId);
            mDb.getShoppingListDao().insert(shoppingListItems);
        });
    }

    public void removeFromShoppingList(Recipe recipe) {
        mAppExecutors.diskIO().execute(() -> {
            final long recipeId = recipe.getId();
            mDb.getShoppingListDao().deleteByRecipeId(recipeId);
        });
    }

    public void removeFromShoppingList(UiShoppingListItem shoppingListItem) {
        mAppExecutors.diskIO().execute(() -> {
            final long shoppingListItemId = shoppingListItem.getId();
            mDb.getShoppingListDao().deleteById(shoppingListItemId);
        });
    }

    public LiveData<List<UiShoppingListItem>> getShoppingListItems() {
        return mShoppingListItems;
    }

    public void updatePortionsInShoppingList(Recipe recipe) {
        Debug.d(this, "Updating portions for '" + recipe.getName() + "' in shopping list");
        mAppExecutors.diskIO().execute(() -> {
            final long recipeId = recipe.getId();
            List<ShoppingListItem> shoppingListItems = mDb.getRecipeIngredientDao().findShoppingListItemByRecipeId(recipeId);
            mDb.getShoppingListDao().deleteByRecipeId(recipeId);
            mDb.getShoppingListDao().insert(shoppingListItems);
        });
    }

    public void updatePortionsInShoppingList(UiShoppingListItem uiShoppingListItem) {
        Debug.d(this, "Updating portions for one item in shopping list");
        mAppExecutors.diskIO().execute(() -> {
            ShoppingListItem shoppingListItem = mDb.getShoppingListDao().findByIdRaw(uiShoppingListItem.getId());
            shoppingListItem.setQuantity(uiShoppingListItem.getQuantity());
            mDb.getShoppingListDao().update(shoppingListItem);
        });
    }

    public LiveData<List<Long>> getRecipeIdsInShoppingList() {
        return mRecipeIdsInShoppingList;
    }

    public void update(UiShoppingListItem... uiShoppingListItems) {
        Debug.d(this, "Updating shopping list item");
        mAppExecutors.diskIO().execute(() -> {
            List<ShoppingListItem> shoppingListItems = new ArrayList<>(uiShoppingListItems.length);
            for (UiShoppingListItem uiShoppingListItem : uiShoppingListItems) {
                ShoppingListItem shoppingListItem = mDb.getShoppingListDao().findByIdRaw(uiShoppingListItem.getId());
                if (shoppingListItem != null) {
                    shoppingListItem.setCompleted(uiShoppingListItem.getCompleted());
                    shoppingListItem.setQuantity(uiShoppingListItem.getQuantity());
                    shoppingListItems.add(shoppingListItem);
                }
            }
            Debug.d(this, shoppingListItems.toString());
            mDb.getShoppingListDao().update(shoppingListItems);
        });
    }

    public LiveData<List<Recipe>> getRecipesInShoppingList() {
        return mRecipesInShoppingList;
    }

    public void deleteAll() {
        mAppExecutors.diskIO().execute(() -> {
            mDb.getTagUsageDao().deleteAll();
            mDb.getRecipeTagDao().deleteAll();
            mDb.getRecipeStepDao().deleteAll();
            mDb.getRecipeIngredientDao().deleteAll();
            mDb.getRecipeDao().deleteAll();
            mDb.getIngredientDao().deleteAll();
            mDb.getUnitDao().deleteAll();
            mDb.getTagDao().deleteAll();
            mDb.getAisleDao().deleteAll();
        });
    }

    public void addToShoppingList(ShoppingListItem item) {
        mAppExecutors.diskIO().execute(() -> mDb.getShoppingListDao().insert(item));
    }

    public LiveData<List<Unit>> getUnits() {
        return mDb.getUnitDao().findAll();
    }

    public void clearCompletedFromShoppingList() {
        mAppExecutors.diskIO().execute(() -> {
            mDb.getShoppingListDao().hideCompletedRecipeItems();
            mDb.getShoppingListDao().deleteCompletedOrphanItems();
        });
    }

    public LiveData<UiShoppingListItem> getShoppingListItemById(long id) {
        return mDb.getShoppingListDao().findById(id);
    }

    public void updateShoppingListItem(Long id, String ingredient, Double quantity, Long unitId) {
        mAppExecutors.diskIO().execute(() -> {
            ShoppingListItem shoppingListItem = mDb.getShoppingListDao().findByIdRaw(id);
            shoppingListItem.setName(ingredient);
            shoppingListItem.setQuantity(quantity);
            shoppingListItem.setUnitId(unitId);
            mDb.getShoppingListDao().update(shoppingListItem);
        });
    }

    public LiveData<List<String>> getUnitsAndIngredientNames() {
        return mDb.getIngredientDao().getUnitsAndIngredientNames();
    }

    public LiveData<List<String>> getIngredientNames() {
        return mDb.getIngredientDao().getIngredientNames();
    }

    public LiveData<List<String>> getUnitNames() {
        return mDb.getUnitDao().getUnitNames();
    }
}
