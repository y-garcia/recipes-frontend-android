package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeDetailDao;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.dao.ShoppingListDao;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipe;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.util.Debug;

import java.util.List;
import java.util.UUID;

public class RecipeDetailRepository {

    private RecipeDao mRecipeDao;
    private RecipeDetailDao mRecipeDetailDao;
    private RecipeIngredientDao mRecipeIngredientDao;
    private ShoppingListDao mShoppingListDao;

    public RecipeDetailRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mRecipeDao = db.getRecipeDao();
        mRecipeDetailDao = db.getRecipeDetailDao();
        mRecipeIngredientDao = db.getRecipeIngredientDao();
        mShoppingListDao = db.getShoppingListDao();
    }

    public LiveData<List<UiRecipe>> getRecipes() {
        return mRecipeDetailDao.findAll();
    }

    public LiveData<UiRecipe> getRecipeById(UUID id) {
        return mRecipeDetailDao.findById(id);
    }

    public LiveData<List<UiRecipeIngredient>> getIngredientsByRecipeId(UUID recipeId) {
        return mRecipeIngredientDao.findByRecipeId(recipeId);
    }

    public void update(Recipe recipe) {
        new UpdateRecipeAsyncTask(mRecipeDao).execute(recipe);
    }

    public LiveData<Boolean> isInShoppingList(UUID recipeId) {
        return mShoppingListDao.isInShoppingList(recipeId);
    }

    public void removeFromShoppingList(UUID recipeId) {
        new RemoveFromShoppingListAsyncTask(mShoppingListDao).execute(recipeId);
    }

    public void addToShoppingList(UUID recipeId) {
        new AddToShoppingListAsyncTask(mShoppingListDao, mRecipeIngredientDao).execute(recipeId);
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

    private static class RemoveFromShoppingListAsyncTask extends AsyncTask<UUID, Void, Void> {

        private ShoppingListDao mAsyncRecipeDao;

        RemoveFromShoppingListAsyncTask(ShoppingListDao dao) {
            mAsyncRecipeDao = dao;
        }

        @Override
        protected Void doInBackground(final UUID... recipeIds) {
            mAsyncRecipeDao.deleteByRecipeId(recipeIds[0]);
            return null;
        }
    }

    private static class AddToShoppingListAsyncTask extends AsyncTask<UUID, Void, Void> {

        private ShoppingListDao shoppingListDao;
        private RecipeIngredientDao recipeIngredientDao;

        AddToShoppingListAsyncTask(ShoppingListDao shoppingListDao, RecipeIngredientDao recipeIngredientDao) {
            this.shoppingListDao = shoppingListDao;
            this.recipeIngredientDao = recipeIngredientDao;
        }

        @Override
        protected Void doInBackground(UUID... recipeIds) {
            Debug.d(this, "Adding recipe with id '" + recipeIds[0] + "' to shopping list");
            final UUID recipeId = recipeIds[0];
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
                RecipeIngredient recipeIngredient = recipeIngredientDao.findByIdRaw(uiRecipeIngredient.getId());
                recipeIngredient.setQuantity(uiRecipeIngredient.getQuantity());
                recipeIngredientDao.update(recipeIngredient);
            }
            return null;
        }
    }
}
