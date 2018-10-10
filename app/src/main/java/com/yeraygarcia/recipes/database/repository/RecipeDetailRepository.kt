package com.yeraygarcia.recipes.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.OnWebResponseListener
import com.yeraygarcia.recipes.database.AppDatabase
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.database.remote.Request
import com.yeraygarcia.recipes.database.remote.ResourceData
import com.yeraygarcia.recipes.database.remote.RetrofitClient
import com.yeraygarcia.recipes.database.remote.Webservice
import com.yeraygarcia.recipes.util.Debug
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import retrofit2.Call
import java.util.*

class RecipeDetailRepository(application: Application) {

    private val context: Context = application
    private val db = AppDatabase.getDatabase(context)
    private val webservice = RetrofitClient.get(context).create(Webservice::class.java)
    private val appExecutors = AppExecutors()

    fun getRecipeById(id: UUID): LiveData<Recipe> {
        return db.recipeDao.findById(id)
    }

    fun getIngredientsByRecipeId(recipeId: UUID): LiveData<List<UiRecipeIngredient>> {
        return db.recipeIngredientDao.findByRecipeId(recipeId)
    }

    fun getStepsByRecipeId(recipeId: UUID): LiveData<List<RecipeStep>> {
        return db.recipeStepDao.findByRecipeId(recipeId)
    }

    fun update(recipe: Recipe) {
        object : Request<Recipe>(context) {
            override fun getEntityBeforeUpdate(newEntity: Recipe): Recipe {
                return db.recipeDao.findRawById(newEntity.id)
            }

            override fun updateLocalDatabase(entity: Recipe) {
                db.recipeDao.update(entity)
            }

            override fun sendRequestToServer(newEntity: Recipe): Call<ResourceData<Recipe>> {
                return webservice.updateRecipe(newEntity)
            }

            override fun onSuccess(responseEntity: Recipe) {
                // do nothing
            }

            override fun onError(errorCode: String, errorMessage: String) {
                // do nothing
            }
        }.send(recipe)
    }

    fun persistDraft(recipe: Recipe, recipeDetailViewModel: RecipeDetailViewModel) {
        object : Request<Recipe>(context) {
            override fun getEntityBeforeUpdate(newEntity: Recipe): Recipe {
                return db.recipeDao.findRawById(newEntity.id)
            }

            override fun updateLocalDatabase(entity: Recipe) {
                db.recipeDao.update(entity)
            }

            override fun sendRequestToServer(newEntity: Recipe): Call<ResourceData<Recipe>> {
                return webservice.updateRecipe(newEntity)
            }

            override fun onSuccess(responseEntity: Recipe) {
                recipeDetailViewModel.recipeDraft = null
            }

            override fun onError(errorCode: String, errorMessage: String) {
                // do nothing
            }
        }.send(recipe)
    }

    fun isInShoppingList(recipeId: UUID): LiveData<Boolean> {
        return db.shoppingListDao.isInShoppingList(recipeId)
    }

    fun removeFromShoppingList(recipeId: UUID) {
        appExecutors.diskIO().execute { db.shoppingListDao.deleteByRecipeId(recipeId) }
    }

    fun addToShoppingList(recipeId: UUID) {
        appExecutors.diskIO().execute {
            Debug.d(this, "Adding recipe with id '$recipeId' to shopping list")
            val shoppingListItems = db.recipeIngredientDao.findShoppingListItemByRecipeId(recipeId)
            db.shoppingListDao.deleteByRecipeId(recipeId)
            db.shoppingListDao.insert(shoppingListItems)
        }
    }

    fun getRecipeStep(id: UUID): LiveData<RecipeStep> {
        return db.recipeStepDao.findById(id)
    }

    fun updateRecipeStep(recipeStep: RecipeStep, listener: OnWebResponseListener) {
        object : Request<RecipeStep>(context) {
            override fun getEntityBeforeUpdate(newEntity: RecipeStep): RecipeStep {
                return db.recipeStepDao.findByIdRaw(newEntity.id)
            }

            override fun updateLocalDatabase(entity: RecipeStep) {
                db.recipeStepDao.update(entity)
            }

            override fun sendRequestToServer(newEntity: RecipeStep): Call<ResourceData<RecipeStep>> {
                return webservice.updateRecipeStep(newEntity)
            }

            override fun onSuccess(responseEntity: RecipeStep) {
                listener.onSuccess()
            }

            override fun onError(errorCode: String, errorMessage: String) {
                listener.onError(errorCode, errorMessage)
            }
        }.send(recipeStep)
    }
}
