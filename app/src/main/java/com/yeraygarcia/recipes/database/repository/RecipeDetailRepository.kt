package com.yeraygarcia.recipes.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.OnWebResponseListener
import com.yeraygarcia.recipes.database.AppDatabase
import com.yeraygarcia.recipes.database.UUIDTypeConverter
import com.yeraygarcia.recipes.database.entity.*
import com.yeraygarcia.recipes.database.entity.Unit
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.database.remote.Request
import com.yeraygarcia.recipes.database.remote.ResourceData
import com.yeraygarcia.recipes.database.remote.RetrofitClient
import com.yeraygarcia.recipes.database.remote.Webservice
import com.yeraygarcia.recipes.util.NetworkUtil
import com.yeraygarcia.recipes.util.toJson
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*

class RecipeDetailRepository(application: Application) {

    private val context: Context = application
    private val db = AppDatabase.getDatabase(context)
    private val webservice = RetrofitClient.get(context).create(Webservice::class.java)
    private val appExecutors = AppExecutors()

    val units: LiveData<List<Unit>>
        get() = db.unitDao.findAll()

    val ingredientNames: LiveData<List<String>>
        get() = db.ingredientDao.ingredientNames

    val unitNames: LiveData<List<String>>
        get() = db.unitDao.unitNames

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
        appExecutors.diskIO { db.shoppingListDao.deleteByRecipeId(recipeId) }
    }

    fun addToShoppingList(recipeId: UUID) {
        appExecutors.diskIO {
            Timber.d("Adding recipe with id '$recipeId' to shopping list")
            val shoppingListItems = db.recipeIngredientDao.findShoppingListItemByRecipeId(recipeId)
            shoppingListItems.forEach { it.id = UUIDTypeConverter.newUUID() }
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

    fun getUiRecipeIngredient(id: UUID): LiveData<UiRecipeIngredient> {
        return db.recipeIngredientDao.findById(id)
    }

    private fun canFetch(): Boolean {
        return NetworkUtil.isOnline(context)
    }

    fun updateRecipeIngredient(id: UUID, ingredientName: String, quantity: Double?, unitId: UUID?) {
        Timber.d("updateRecipeIngredient(id, ingredientName, quantity, unitId)")
        if (canFetch()) {
            appExecutors.diskIO {
                var ingredientId: UUID? = db.ingredientDao.getIngredientIdByName(ingredientName)
                if (ingredientId == null) {
                    val ingredient = Ingredient(name = ingredientName)
                    ingredientId = ingredient.id
                    db.ingredientDao.insert(ingredient)
                }
                val oldRecipeIngredient = db.recipeIngredientDao.findByIdRaw(id)
                val newRecipeIngredient = oldRecipeIngredient.copy(
                    ingredientId = ingredientId,
                    quantity = quantity,
                    unitId = unitId
                )

                db.recipeIngredientDao.update(newRecipeIngredient)

                Timber.d("Sending request to server.")
                Timber.d(newRecipeIngredient.toString())

                val call = webservice.updateRecipeIngredient(newRecipeIngredient)

                call.enqueue(object : Callback<ResourceData<RecipeIngredient>> {
                    override fun onResponse(
                        call: Call<ResourceData<RecipeIngredient>>,
                        response: Response<ResourceData<RecipeIngredient>>
                    ) {
                        Timber.d("We got a response from the server: ${response.toJson()}")
                        if (response.isSuccessful) {
                            Timber.d("Response was successful: ${response.code()}")
                            val body = response.body()
                            if (body?.result != null) {
                                Timber.d("Body contains payload. Update database.")
                            } else {
                                Timber.d("Body was empty. Abort.")
                                appExecutors.diskIO {
                                    db.recipeIngredientDao.update(
                                        oldRecipeIngredient
                                    )
                                }
                            }
                        } else {
                            Timber.d("Response was not successful: ${response.code()} - ${response.errorBody()}")
                            appExecutors.diskIO { db.recipeIngredientDao.update(oldRecipeIngredient) }
                        }
                    }

                    override fun onFailure(
                        call: Call<ResourceData<RecipeIngredient>>,
                        t: Throwable
                    ) {
                        Timber.d("Call failed: ${t.message}")
                        appExecutors.diskIO {
                            db.recipeIngredientDao.update(oldRecipeIngredient)
                        }
                    }
                })
            }
        }
    }

}
