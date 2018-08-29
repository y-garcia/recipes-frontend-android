package com.yeraygarcia.recipes.database.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.yeraygarcia.recipes.OnWebResponseListener;
import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.dao.RecipeIngredientDao;
import com.yeraygarcia.recipes.database.dao.ShoppingListDao;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.database.remote.Request;
import com.yeraygarcia.recipes.database.remote.ResourceData;
import com.yeraygarcia.recipes.database.remote.RetrofitClient;
import com.yeraygarcia.recipes.database.remote.Webservice;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;

public class RecipeDetailRepository {

    private Context mContext;
    private AppDatabase mDb;
    private Webservice mWebservice;

    public RecipeDetailRepository(Application application) {
        mContext = application;
        mDb = AppDatabase.getDatabase(application);
        mWebservice = RetrofitClient.get(mContext).create(Webservice.class);
    }

    public LiveData<Recipe> getRecipeById(UUID id) {
        return mDb.getRecipeDao().findById(id);
    }

    public LiveData<List<UiRecipeIngredient>> getIngredientsByRecipeId(UUID recipeId) {
        return mDb.getRecipeIngredientDao().findByRecipeId(recipeId);
    }

    public LiveData<List<RecipeStep>> getStepsByRecipeId(UUID recipeId) {
        return mDb.getRecipeStepDao().findByRecipeId(recipeId);
    }

    public void update(Recipe recipe) {
        new Request<Recipe>(mContext) {
            @Override
            public Recipe getEntityBeforeUpdate(Recipe newEntity) {
                return mDb.getRecipeDao().findRawById(newEntity.getId());
            }

            @Override
            public void updateLocalDatabase(Recipe entity) {
                mDb.getRecipeDao().update(entity);
            }

            @NotNull
            @Override
            public Call<ResourceData<Recipe>> sendRequestToServer(Recipe newEntity) {
                return mWebservice.updateRecipe(newEntity);
            }

            @Override
            public void onSuccess(Recipe result) {
                // do nothing
            }

            @Override
            public void onError(@NonNull String errorCode, @NonNull String errorMessage) {
                // do nothing
            }
        }.send(recipe);
    }

    public void persistDraft(Recipe recipe, RecipeDetailViewModel recipeDetailViewModel) {
        new Request<Recipe>(mContext) {
            @Override
            public Recipe getEntityBeforeUpdate(Recipe newEntity) {
                return mDb.getRecipeDao().findRawById(newEntity.getId());
            }

            @Override
            public void updateLocalDatabase(Recipe entity) {
                mDb.getRecipeDao().update(entity);
            }

            @NotNull
            @Override
            public Call<ResourceData<Recipe>> sendRequestToServer(Recipe newEntity) {
                return mWebservice.updateRecipe(newEntity);
            }

            @Override
            public void onSuccess(Recipe result) {
                recipeDetailViewModel.setRecipeDraft(null);
            }

            @Override
            public void onError(@NonNull String errorCode, @NonNull String errorMessage) {
                // do nothing
            }
        }.send(recipe);
    }

    public LiveData<Boolean> isInShoppingList(UUID recipeId) {
        return mDb.getShoppingListDao().isInShoppingList(recipeId);
    }

    public void removeFromShoppingList(UUID recipeId) {
        new RemoveFromShoppingListAsyncTask(mDb.getShoppingListDao()).execute(recipeId);
    }

    public void addToShoppingList(UUID recipeId) {
        new AddToShoppingListAsyncTask(mDb.getShoppingListDao(), mDb.getRecipeIngredientDao()).execute(recipeId);
    }

    public void update(UiRecipeIngredient... ingredients) {
        new UpdateIngredientAsyncTask(mDb.getRecipeIngredientDao()).execute(ingredients);
    }

    public LiveData<RecipeStep> getRecipeStep(UUID id) {
        return mDb.getRecipeStepDao().findById(id);
    }

    public void updateRecipeStep(RecipeStep recipeStep, OnWebResponseListener listener) {
        new Request<RecipeStep>(mContext) {
            @Override
            public RecipeStep getEntityBeforeUpdate(RecipeStep newEntity) {
                return mDb.getRecipeStepDao().findByIdRaw(newEntity.getId());
            }

            @Override
            public void updateLocalDatabase(RecipeStep entity) {
                mDb.getRecipeStepDao().update(entity);
            }

            @NotNull
            @Override
            public Call<ResourceData<RecipeStep>> sendRequestToServer(RecipeStep newEntity) {
                return mWebservice.updateRecipeStep(newEntity);
            }

            @Override
            public void onSuccess(RecipeStep responseEntity) {
                listener.onSuccess();
            }

            @Override
            public void onError(@NonNull String errorCode, @NonNull String errorMessage) {
                listener.onError(errorCode, errorMessage);
            }
        }.send(recipeStep);
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
