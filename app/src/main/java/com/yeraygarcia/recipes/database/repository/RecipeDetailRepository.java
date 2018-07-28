package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeDetailDao;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.dao.ShoppingListDao;
import com.yeraygarcia.recipes.database.dao.UnitDao;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipe;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.util.Debug;

import java.util.List;

public class RecipeDetailRepository {

    private RecipeDao mRecipeDao;
    private RecipeDetailDao mRecipeDetailDao;
    private RecipeIngredientDao mRecipeIngredientDao;
    private ShoppingListDao mShoppingListDao;
    private UnitDao mUnitDao;

    public RecipeDetailRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mRecipeDao = db.getRecipeDao();
        mRecipeDetailDao = db.getRecipeDetailDao();
        mRecipeIngredientDao = db.getRecipeIngredientDao();
        mShoppingListDao = db.getShoppingListDao();
        mUnitDao = db.getUnitDao();
    }

    public LiveData<List<UiRecipe>> getRecipes() {
        return mRecipeDetailDao.findAll();
    }

    public LiveData<UiRecipe> getRecipeById(long id) {
        return mRecipeDetailDao.findById(id);
    }

    public LiveData<List<UiRecipeIngredient>> getIngredientsByRecipeId(long recipeId) {
        return mRecipeIngredientDao.findByRecipeId(recipeId);
    }

    public void update(Recipe recipe) {
        new UpdateRecipeAsyncTask(mRecipeDao).execute(recipe);
    }

    public LiveData<Boolean> isInShoppingList(long recipeId) {
        return mShoppingListDao.isInShoppingList(recipeId);
    }

    public void removeFromShoppingList(long recipeId) {
        new RemoveFromShoppingListAsyncTask(mShoppingListDao).execute(recipeId);
    }

    public void addToShoppingList(long recipeId) {
        new AddToShoppingListAsyncTask(mShoppingListDao, mRecipeIngredientDao).execute(recipeId);
    }

    public LiveData<List<String>> getUnitPluralNames() {
        return mUnitDao.findAllPluralNames();
    }

    public void update(UiRecipeIngredient... ingredients) {
        new UpdateIngredientAsyncTask(mRecipeIngredientDao).execute(ingredients);
    }

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

    private static class RemoveFromShoppingListAsyncTask extends AsyncTask<Long, Void, Void> {

        private ShoppingListDao mAsyncRecipeDao;

        RemoveFromShoppingListAsyncTask(ShoppingListDao dao) {
            mAsyncRecipeDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... recipeIds) {
            mAsyncRecipeDao.deleteByRecipeId(recipeIds[0]);
            return null;
        }
    }

    private static class AddToShoppingListAsyncTask extends AsyncTask<Long, Void, Void> {

        private ShoppingListDao shoppingListDao;
        private RecipeIngredientDao recipeIngredientDao;

        AddToShoppingListAsyncTask(ShoppingListDao shoppingListDao, RecipeIngredientDao recipeIngredientDao) {
            this.shoppingListDao = shoppingListDao;
            this.recipeIngredientDao = recipeIngredientDao;
        }

        @Override
        protected Void doInBackground(Long... recipeIds) {
            Debug.d(this, "Adding recipe with id '" + recipeIds[0] + "' to shopping list");
            final long recipeId = recipeIds[0];
            List<ShoppingListItem> shoppingListItems = recipeIngredientDao.findShoppingListItemByRecipeId(recipeId);
            shoppingListDao.deleteByRecipeId(recipeId);
            shoppingListDao.insert(shoppingListItems);
            return null;
        }
    }

    private static class UpdateIngredientAsyncTask extends AsyncTask<UiRecipeIngredient, Void, Void> {

        private RecipeIngredientDao recipeIngredientDao;

        UpdateIngredientAsyncTask(RecipeIngredientDao dao) {
            recipeIngredientDao = dao;
        }

        @Override
        protected Void doInBackground(final UiRecipeIngredient... ingredients) {
            for (UiRecipeIngredient uiRecipeIngredient : ingredients) {
                Debug.d(this, "Saving ingredient " + uiRecipeIngredient.toString());
                RecipeIngredient recipeIngredient = recipeIngredientDao.findById(uiRecipeIngredient.getId());
                recipeIngredient.setQuantity(uiRecipeIngredient.getQuantity());
                recipeIngredientDao.update(recipeIngredient);
            }
            return null;
        }
    }
}
