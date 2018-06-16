package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.dao.RecipeTagDao;
import com.yeraygarcia.recipes.database.dao.ShoppingListDao;
import com.yeraygarcia.recipes.database.dao.TagDao;
import com.yeraygarcia.recipes.database.dao.TagUsageDao;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.TagUsage;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.util.Debug;

import java.util.List;

public class RecipeRepository {

    private RecipeDao mRecipeDao;
    private RecipeTagDao mRecipeTagDao;
    private TagUsageDao mTagUsageDao;
    private TagDao mTagDao;
    private RecipeIngredientDao mRecipeIngredientDao;
    private ShoppingListDao mShoppingListDao;

    private LiveData<List<Recipe>> mRecipes;
    private LiveData<List<Tag>> mTags;
    private LiveData<List<UiShoppingListItem>> mShoppingListItems;
    private LiveData<List<Long>> mRecipeIdsInShoppingList;

    public RecipeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mRecipeDao = db.getRecipeDao();
        mRecipeTagDao = db.getRecipeTagDao();
        mTagUsageDao = db.getTagUsageDao();
        mTagDao = db.getTagDao();
        mRecipeIngredientDao = db.getRecipeIngredientDao();
        mShoppingListDao = db.getShoppingListDao();

        mRecipes = mRecipeDao.findAll();
        mTags = mTagDao.findAll();
        mShoppingListItems = mShoppingListDao.findAll();
        mRecipeIdsInShoppingList = mShoppingListDao.findDistinctRecipeIds();
    }

    // Methods

    public LiveData<List<Recipe>> getRecipes() {
        return mRecipes;
    }

    public LiveData<List<Tag>> getTags() {
        return mTags;
    }

    public LiveData<List<Recipe>> getRecipesByTagId(List<Long> tagIds) {
        if (tagIds.size() == 0) {
            return mRecipeDao.findAll();
        }
        return mRecipeTagDao.findRecipesByAllTagId(tagIds, tagIds.size());
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
        new ShoppingListUpdatePortionsAsyncTask(mShoppingListDao, mRecipeIngredientDao).execute(shoppingListItem);
    }

    public LiveData<List<Long>> getRecipeIdsInShoppingList() {
        return mRecipeIdsInShoppingList;
    }

    public void update(UiShoppingListItem shoppingListItem) {
        new UpdateShoppingListItemAsyncTask(mShoppingListDao).execute(shoppingListItem);
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
        private RecipeIngredientDao recipeIngredientDao;

        ShoppingListUpdatePortionsAsyncTask(ShoppingListDao shoppingListDao, RecipeIngredientDao recipeIngredientDao) {
            this.shoppingListDao = shoppingListDao;
            this.recipeIngredientDao = recipeIngredientDao;
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
        protected Void doInBackground(final UiShoppingListItem... shoppingListItems) {
            Debug.d(this, "Marking item in shopping list (in)complete");
            UiShoppingListItem uiShoppingListItem = shoppingListItems[0];
            ShoppingListItem shoppingListItem = shoppingListDao.findById(uiShoppingListItem.getId());
            shoppingListItem.setCompleted(uiShoppingListItem.getCompleted());
            shoppingListDao.update(shoppingListItem);
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
