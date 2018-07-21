package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yeraygarcia.recipes.AppExecutors;
import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.dao.RecipeTagDao;
import com.yeraygarcia.recipes.database.dao.ShoppingListDao;
import com.yeraygarcia.recipes.database.dao.TagDao;
import com.yeraygarcia.recipes.database.dao.TagUsageDao;
import com.yeraygarcia.recipes.database.entity.Aisle;
import com.yeraygarcia.recipes.database.entity.Ingredient;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.RecipeTag;
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

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class RecipeRepository {

    private Webservice mWebservice;

    private final AppExecutors appExecutors;

    private AppDatabase mDb;

    private RecipeDao mRecipeDao;
    private RecipeTagDao mRecipeTagDao;
    private TagUsageDao mTagUsageDao;
    private TagDao mTagDao;
    private RecipeIngredientDao mRecipeIngredientDao;
    private ShoppingListDao mShoppingListDao;

    private LiveData<List<UiShoppingListItem>> mShoppingListItems;
    private LiveData<List<Long>> mRecipeIdsInShoppingList;
    private LiveData<List<Recipe>> mRecipesInShoppingList;

    public RecipeRepository(Application application) {
        mDb = AppDatabase.getDatabase(application);

        mWebservice = RetrofitInstance.getRetrofitInstance().create(Webservice.class);
        appExecutors = new AppExecutors();

        mRecipeDao = mDb.getRecipeDao();
        mRecipeTagDao = mDb.getRecipeTagDao();
        mTagUsageDao = mDb.getTagUsageDao();
        mTagDao = mDb.getTagDao();
        mRecipeIngredientDao = mDb.getRecipeIngredientDao();
        mShoppingListDao = mDb.getShoppingListDao();

        mShoppingListItems = mShoppingListDao.findAll();
        mRecipeIdsInShoppingList = mShoppingListDao.findDistinctRecipeIds();
        mRecipesInShoppingList = mRecipeDao.findRecipesInShoppingList();
    }

    // Methods

    public void refreshRecipes() {
        Debug.d(this, "refreshRecipes()");
        LiveData<ApiResponse<ResourceData<All>>> apiResponse = mWebservice.getAll();
        ApiResponse<ResourceData<All>> response = apiResponse.getValue();

        if (response != null && response.isSuccessful()) {
            appExecutors.diskIO().execute(() -> {
                if (response.body != null) {
                    All data = response.body.getResult();
                    mDb.getAisleDao().upsert(data.getAisles());
                    mDb.getUnitDao().upsert(data.getUnits());
                    mDb.getTagDao().upsert(data.getTags());
                    mDb.getIngredientDao().upsert(data.getIngredients());
                    mDb.getRecipeDao().upsert(data.getRecipes());
                    mDb.getRecipeIngredientDao().upsert(data.getRecipeIngredients());
                    mDb.getRecipeStepDao().upsert(data.getRecipeSteps());
                    mDb.getRecipeTagDao().upsert(data.getRecipeTags());
                }
            });
        }
    }

    public LiveData<Resource<List<Recipe>>> getRecipes() {
        return getRecipes(false);
    }

    public LiveData<Resource<List<Recipe>>> getRecipes(boolean forceFetch) {
        return new NetworkBoundResource<List<Recipe>, ResourceData<All>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull ResourceData<All> callResult) {
                Debug.d(this, "getRecipes(" + forceFetch + ").saveCallResult(callResult)");
                All data = callResult.getResult();
                mDb.getAisleDao().upsert(data.getAisles());
                mDb.getUnitDao().upsert(data.getUnits());
                mDb.getTagDao().upsert(data.getTags());
                mDb.getIngredientDao().upsert(data.getIngredients());
                mDb.getRecipeDao().upsert(data.getRecipes());
                mDb.getRecipeIngredientDao().upsert(data.getRecipeIngredients());
                mDb.getRecipeStepDao().upsert(data.getRecipeSteps());
                mDb.getRecipeTagDao().upsert(data.getRecipeTags());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Recipe> data) {
                Debug.d(this, "getRecipes(" + forceFetch + ").shouldFetch(" + (data == null ? "null" : "[" + data.size() + "]") + ")");
                return forceFetch || (data == null || data.isEmpty());
            }

            @NonNull
            @Override
            protected LiveData<List<Recipe>> loadFromDb() {
                Debug.d(this, "getRecipes(" + forceFetch + ").loadFromDb()");
                return mDb.getRecipeDao().findAll();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResourceData<All>>> createCall() {
                Debug.d(this, "getRecipes(" + forceFetch + ").createCall()");
                return mWebservice.getAll();
            }
        }.asLiveData();
    }

//    public LiveData<Resource<List<Tag>>> getTags() {
//        return new NetworkBoundResource<List<Tag>, ResourceData<List<Tag>>>(appExecutors) {
//            @Override
//            protected void saveCallResult(@NonNull ResourceData<List<Tag>> callResult) {
//                mTagDao.save(callResult.getResult().toArray(new Tag[callResult.getResult().size()]));
//            }
//
//            @Override
//            protected boolean shouldFetch(@Nullable List<Tag> data) {
//                return data == null || data.isEmpty();
//            }
//
//            @NonNull
//            @Override
//            protected LiveData<List<Tag>> loadFromDb() {
//                return mTagDao.findAll();
//            }
//
//            @NonNull
//            @Override
//            protected LiveData<ApiResponse<ResourceData<List<Tag>>>> createCall() {
//                return mWebservice.getTags();
//            }
//        }.asLiveData();
//    }

    public LiveData<List<Tag>> getTags() {
        Debug.d(this, "getTags()");
        return mTagDao.findAll();
    }

    public LiveData<Resource<List<Recipe>>> getRecipesByTagId(List<Long> tagIds) {
        return getRecipesByTagId(tagIds, true);
    }

    public LiveData<Resource<List<Recipe>>> getRecipesByTagId(List<Long> tagIds, boolean forceFetch) {
        Debug.d(this, "getRecipesByTagId(" + tagIds + ")");

        if (tagIds.size() == 0) {
            return getRecipes(forceFetch);
        }

        return new NetworkBoundResource<List<Recipe>, ResourceData<All>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull ResourceData<All> callResult) {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ", " + forceFetch + ").saveCallResult(callResult)");
                All data = callResult.getResult();
                mDb.getAisleDao().save(data.getAisles().toArray(new Aisle[]{}));
                mDb.getUnitDao().save(data.getUnits().toArray(new Unit[]{}));
                mDb.getTagDao().save(data.getTags().toArray(new Tag[]{}));
                mDb.getIngredientDao().save(data.getIngredients().toArray(new Ingredient[]{}));
                mDb.getRecipeDao().save(data.getRecipes().toArray(new Recipe[]{}));
                mDb.getRecipeIngredientDao().save(data.getRecipeIngredients().toArray(new RecipeIngredient[]{}));
                mDb.getRecipeStepDao().save(data.getRecipeSteps().toArray(new RecipeStep[]{}));
                mDb.getRecipeTagDao().save(data.getRecipeTags().toArray(new RecipeTag[]{}));
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Recipe> data) {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ", " + forceFetch + ").shouldFetch(" + (data == null ? "null" : "[" + data.size() + "]") + ")");
                return forceFetch || (data == null || data.isEmpty());
            }

            @NonNull
            @Override
            protected LiveData<List<Recipe>> loadFromDb() {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ", " + forceFetch + ").loadFromDb()");
                return mRecipeTagDao.findRecipesByAllTagIds(tagIds, tagIds.size());
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResourceData<All>>> createCall() {
                Debug.d(this, "getRecipesByTagId(" + tagIds + ", " + forceFetch + ").createCall()");
                return mWebservice.getAll();
            }
        }.asLiveData();
    }

    public void insert(Recipe recipe) {
        new RecipeInsertAsyncTask(mRecipeDao).execute(recipe);
    }

    public void logTagUsage(Long tagId) {
        TagUsage tagUsage = new TagUsage(tagId);
        new TagUsageInsertAsyncTask(mTagUsageDao).execute(tagUsage);
    }

    public void updateTagUsage() {
        new TagUpdateAsyncTask(mTagUsageDao, mTagDao).execute();
    }

    public void addToShoppingList(Recipe recipe) {
        new ShoppingListInsertAsyncTask(mShoppingListDao, mRecipeIngredientDao).execute(recipe);
    }

    public void removeFromShoppingList(Recipe recipe) {
        new ShoppingListDeleteRecipeAsyncTask(mShoppingListDao).execute(recipe);
    }

    public void removeFromShoppingList(UiShoppingListItem shoppingListItem) {
        new ShoppingListDeleteItemAsyncTask(mShoppingListDao).execute(shoppingListItem);
    }

    public LiveData<List<UiShoppingListItem>> getShoppingListItems() {
        return mShoppingListItems;
    }

    public void update(Recipe recipe) {
        new UpdateRecipeAsyncTask(mRecipeDao).execute(recipe);
    }

    public void updatePortionsInShoppingList(Recipe recipe) {
        new ShoppingListUpdateRecipeAsyncTask(mShoppingListDao, mRecipeIngredientDao).execute(recipe);
    }

    public void updatePortionsInShoppingList(UiShoppingListItem shoppingListItem) {
        new ShoppingListUpdatePortionsAsyncTask(mShoppingListDao).execute(shoppingListItem);
    }

    public LiveData<List<Long>> getRecipeIdsInShoppingList() {
        return mRecipeIdsInShoppingList;
    }

    public void update(UiShoppingListItem... shoppingListItems) {
        new UpdateShoppingListItemAsyncTask(mShoppingListDao).execute(shoppingListItems);
    }

    public LiveData<List<Recipe>> getRecipesInShoppingList() {
        return mRecipesInShoppingList;
    }

    public void deleteAll() {
        appExecutors.diskIO().execute(() -> {
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

    // Internal classes

    private static class UpdateRecipeAsyncTask extends AsyncTask<Recipe, Void, Void> {

        private RecipeDao mAsyncRecipeDao;

        UpdateRecipeAsyncTask(RecipeDao dao) {
            mAsyncRecipeDao = dao;
        }

        @Override
        protected Void doInBackground(final Recipe... params) {
            mAsyncRecipeDao.update(params[0]);
            return null;
        }
    }

    private static class RecipeInsertAsyncTask extends AsyncTask<Recipe, Void, Void> {
        private RecipeDao mDao;

        RecipeInsertAsyncTask(RecipeDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(final Recipe... params) {
            mDao.insert(params);
            return null;
        }
    }

    private static class TagUsageInsertAsyncTask extends AsyncTask<TagUsage, Void, Void> {
        private TagUsageDao mDao;

        TagUsageInsertAsyncTask(TagUsageDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(final TagUsage... params) {
            mDao.insert(params);
            return null;
        }
    }

    private static class TagUpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private TagUsageDao tagUsageDao;
        private TagDao tagDao;

        TagUpdateAsyncTask(TagUsageDao tagUsageDao, TagDao tagDao) {
            this.tagUsageDao = tagUsageDao;
            this.tagDao = tagDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<Tag> tags = tagUsageDao.getTagsWithUpdatedUsage();
            tagDao.update(tags.toArray(new Tag[]{}));
            return null;
        }
    }

    private static class ShoppingListInsertAsyncTask extends AsyncTask<Recipe, Void, Void> {
        private ShoppingListDao shoppingListDao;
        private RecipeIngredientDao recipeIngredientDao;

        ShoppingListInsertAsyncTask(ShoppingListDao shoppingListDao, RecipeIngredientDao recipeIngredientDao) {
            this.shoppingListDao = shoppingListDao;
            this.recipeIngredientDao = recipeIngredientDao;
        }

        @Override
        protected Void doInBackground(Recipe... recipes) {
            Debug.d(this, "Adding '" + recipes[0].getName() + "' to shopping list");
            final long recipeId = recipes[0].getId();
            List<ShoppingListItem> shoppingListItems = recipeIngredientDao.findShoppingListItemByRecipeId(recipeId);
            shoppingListDao.deleteByRecipeId(recipeId);
            shoppingListDao.insert(shoppingListItems.toArray(new ShoppingListItem[]{}));
            return null;
        }
    }

    private static class ShoppingListUpdateRecipeAsyncTask extends AsyncTask<Recipe, Void, Void> {
        private ShoppingListDao shoppingListDao;
        private RecipeIngredientDao recipeIngredientDao;

        ShoppingListUpdateRecipeAsyncTask(ShoppingListDao shoppingListDao, RecipeIngredientDao recipeIngredientDao) {
            this.shoppingListDao = shoppingListDao;
            this.recipeIngredientDao = recipeIngredientDao;
        }

        @Override
        protected Void doInBackground(Recipe... recipes) {
            Debug.d(this, "Updating portions for '" + recipes[0].getName() + "' in shopping list");
            final long recipeId = recipes[0].getId();
            List<ShoppingListItem> shoppingListItems = recipeIngredientDao.findShoppingListItemByRecipeId(recipeId);
            shoppingListDao.deleteByRecipeId(recipeId);
            shoppingListDao.insert(shoppingListItems.toArray(new ShoppingListItem[]{}));
            return null;
        }
    }

    private static class ShoppingListUpdatePortionsAsyncTask extends AsyncTask<UiShoppingListItem, Void, Void> {
        private ShoppingListDao shoppingListDao;

        ShoppingListUpdatePortionsAsyncTask(ShoppingListDao shoppingListDao) {
            this.shoppingListDao = shoppingListDao;
        }

        @Override
        protected Void doInBackground(UiShoppingListItem... shoppingListItems) {
            Debug.d(this, "Updating portions for one item in shopping list");
            UiShoppingListItem uiShoppingListItem = shoppingListItems[0];
            ShoppingListItem shoppingListItem = shoppingListDao.findById(uiShoppingListItem.getId());
            shoppingListItem.setQuantity(uiShoppingListItem.getQuantity());
            shoppingListDao.update(shoppingListItem);
            return null;
        }
    }

    private static class UpdateShoppingListItemAsyncTask extends AsyncTask<UiShoppingListItem, Void, Void> {

        private ShoppingListDao shoppingListDao;

        UpdateShoppingListItemAsyncTask(ShoppingListDao dao) {
            shoppingListDao = dao;
        }

        @Override
        protected Void doInBackground(final UiShoppingListItem... uiShoppingListItems) {
            Debug.d(this, "Updating shopping list item");
            ShoppingListItem[] shoppingListItems = new ShoppingListItem[uiShoppingListItems.length];
            for (int i = 0; i < uiShoppingListItems.length; i++) {
                UiShoppingListItem uiShoppingListItem = uiShoppingListItems[i];
                ShoppingListItem shoppingListItem = shoppingListDao.findById(uiShoppingListItem.getId());
                shoppingListItem.setCompleted(uiShoppingListItem.getCompleted());
                shoppingListItem.setQuantity(uiShoppingListItem.getQuantity());
                shoppingListItems[i] = shoppingListItem;
            }
            Debug.d(this, Arrays.toString(shoppingListItems));
            shoppingListDao.update(shoppingListItems);
            return null;
        }
    }

    private static class ShoppingListDeleteRecipeAsyncTask extends AsyncTask<Recipe, Void, Void> {
        private ShoppingListDao shoppingListDao;

        ShoppingListDeleteRecipeAsyncTask(ShoppingListDao shoppingListDao) {
            this.shoppingListDao = shoppingListDao;
        }

        @Override
        protected Void doInBackground(Recipe... recipes) {
            final long recipeId = recipes[0].getId();
            shoppingListDao.deleteByRecipeId(recipeId);
            return null;
        }
    }

    private static class ShoppingListDeleteItemAsyncTask extends AsyncTask<UiShoppingListItem, Void, Void> {
        private ShoppingListDao shoppingListDao;

        ShoppingListDeleteItemAsyncTask(ShoppingListDao shoppingListDao) {
            this.shoppingListDao = shoppingListDao;
        }

        @Override
        protected Void doInBackground(UiShoppingListItem... shoppingListItems) {
            final long shoppingListItemId = shoppingListItems[0].getId();
            shoppingListDao.deleteById(shoppingListItemId);
            return null;
        }
    }
}
