package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeDao;
import com.yeraygarcia.recipes.database.dao.RecipeDetailDao;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipe;

import java.util.List;

public class RecipeDetailRepository {

    private RecipeDao mRecipeDao;
    private RecipeDetailDao mRecipeDetailDao;
    private RecipeIngredientDao mRecipeIngredientDao;

    public RecipeDetailRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mRecipeDao = db.getRecipeDao();
        mRecipeDetailDao = db.getRecipeDetailDao();
        mRecipeIngredientDao = db.getRecipeIngredientDao();
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
        new updateAsyncTask(mRecipeDao).execute(recipe);
    }

    private static class updateAsyncTask extends AsyncTask<Recipe, Void, Void> {

        private RecipeDao mAsyncRecipeDao;

        updateAsyncTask(RecipeDao dao) {
            mAsyncRecipeDao = dao;
        }

        @Override
        protected Void doInBackground(final Recipe... params) {
            mAsyncRecipeDao.update(params[0]);
            return null;
        }
    }
}
