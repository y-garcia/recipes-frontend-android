package com.yeraygarcia.recipes.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.gson.Gson
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.database.AppDatabase
import com.yeraygarcia.recipes.database.entity.*
import com.yeraygarcia.recipes.database.entity.Unit
import com.yeraygarcia.recipes.database.entity.custom.All
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import com.yeraygarcia.recipes.database.remote.*
import com.yeraygarcia.recipes.util.Debug
import com.yeraygarcia.recipes.util.NetworkUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Singleton

@Singleton
class RecipeRepository(application: Application) {

    private val context: Context = application
    private val appExecutors = AppExecutors()
    private val webservice = RetrofitClient.get(context).create(Webservice::class.java)
    private val db = AppDatabase.getDatabase(context)

    val shoppingListItems = db.shoppingListDao.findAll()
    val recipeIdsInShoppingList = db.shoppingListDao.findDistinctRecipeIds()
    val recipesInShoppingList = db.recipeDao.findRecipesInShoppingList()

    private var cacheIsDirty = true

    val recipes: LiveData<Resource<List<Recipe>>>
        get() = object : NetworkBoundResource<List<Recipe>, ResourceData<All>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Recipe>> {
                Debug.d("RecipeRespository.NetworkBoundResource", "getRecipes().loadFromDb()")
                return db.recipeDao.findAll()
            }

            override fun shouldFetch(data: List<Recipe>?): Boolean {
                Debug.d(
                    "RecipeRespository.NetworkBoundResource",
                    "getRecipes().shouldFetch(" + (if (data == null) "null" else "[" + data.size + "]") + ")"
                )
                return (data == null || data.isEmpty() || cacheIsDirty) && canFetch()
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<All>>> {
                Debug.d("RecipeRespository.NetworkBoundResource", "getRecipes().createCall()")
                return webservice.getAll()
            }

            override fun saveCallResult(callResult: ResourceData<All>) {
                Debug.d(
                    "RecipeRespository.NetworkBoundResource",
                    "getRecipes().saveCallResult(callResult)"
                )
                updateAllFromResource(callResult)
            }
        }.asLiveData()

    val tags: LiveData<List<Tag>>
        get() {
            Debug.d(this, "getTags()")
            return db.tagDao.findAll()
        }

    val units: LiveData<List<Unit>>
        get() = db.unitDao.findAll()

    val unitsAndIngredientNames: LiveData<List<String>>
        get() = db.ingredientDao.unitsAndIngredientNames

    val ingredientNames: LiveData<List<String>>
        get() = db.ingredientDao.ingredientNames

    val unitNames: LiveData<List<String>>
        get() = db.unitDao.unitNames

    // Methods

    private fun canFetch(): Boolean {
        return NetworkUtil.isOnline(context)
    }

    private fun updateAllFromResource(resource: ResourceData<All>) {
        val data = resource.result

        data?.apply {
            db.recipeTagDao.deleteIfNotIn(recipeTags)
            db.recipeStepDao.deleteIfNotIn(recipeSteps)
            db.recipeIngredientDao.deleteIfNotIn(recipeIngredients)
            db.recipeDao.deleteIfNotIn(recipes)
            db.ingredientDao.deleteIfNotIn(ingredients)
            db.tagDao.deleteIfNotIn(tags)
            db.unitDao.deleteIfNotIn(units)
            db.aisleDao.deleteIfNotIn(aisles)

            db.aisleDao.upsert(aisles)
            db.unitDao.upsert(units)
            db.tagDao.upsert(tags)
            db.ingredientDao.upsert(ingredients)
            db.recipeDao.upsert(recipes)
            db.recipeIngredientDao.upsert(recipeIngredients)
            db.recipeStepDao.upsert(recipeSteps)
            db.recipeTagDao.upsert(recipeTags)
        }

        updateTagUsage()

        db.lastUpdateDao.upsert(LastUpdate(System.currentTimeMillis()))
        cacheIsDirty = false
    }

    fun invalidateCache() {
        cacheIsDirty = true
    }

    fun getRecipesByTagId(tagIds: List<UUID>): LiveData<Resource<List<Recipe>>> {
        Debug.d(this, "getRecipesByTagId($tagIds)")

        return if (tagIds.isEmpty()) {
            recipes
        } else object : NetworkBoundResource<List<Recipe>, ResourceData<All>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Recipe>> {
                Debug.d(
                    "RecipeRespository.NetworkBoundResource",
                    "getRecipesByTagId($tagIds).loadFromDb()"
                )
                return db.recipeTagDao.findRecipesByAllTagIds(tagIds, tagIds.size)
            }

            override fun shouldFetch(data: List<Recipe>?): Boolean {
                Debug.d(
                    "RecipeRespository.NetworkBoundResource",
                    "getRecipesByTagId(" + tagIds + ").shouldFetch(" + (if (data == null) "null" else "[" + data.size + "]") + ")"
                )
                return cacheIsDirty && canFetch()
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<All>>> {
                Debug.d(
                    "RecipeRespository.NetworkBoundResource",
                    "getRecipesByTagId($tagIds).createCall()"
                )
                return webservice.getAll()
            }

            override fun saveCallResult(callResult: ResourceData<All>) {
                Debug.d(
                    "RecipeRespository.NetworkBoundResource",
                    "getRecipesByTagId($tagIds).saveCallResult(callResult)"
                )
                updateAllFromResource(callResult)
            }
        }.asLiveData()

    }

    fun update(recipe: Recipe) {
        appExecutors.diskIO().execute { db.recipeDao.update(recipe) }
    }

    fun logTagUsage(tagId: UUID) {
        val tagUsage = TagUsage(tagId = tagId)
        appExecutors.diskIO().execute { db.tagUsageDao.insert(tagUsage) }
    }

    fun updateTagUsage() {
        appExecutors.diskIO().execute {
            val tags = db.tagUsageDao.tagsWithUpdatedUsage
            db.tagDao.update(tags)
        }
    }

    fun addToShoppingList(recipe: Recipe) {
        Debug.d(this, "Adding '" + recipe.name + "' to shopping list")
        appExecutors.diskIO().execute {
            val shoppingListItems = db.recipeIngredientDao.findShoppingListItemByRecipeId(recipe.id)
            db.shoppingListDao.deleteByRecipeId(recipe.id)
            db.shoppingListDao.insert(shoppingListItems)
        }
    }

    fun removeFromShoppingList(recipe: Recipe) {
        appExecutors.diskIO().execute { db.shoppingListDao.deleteByRecipeId(recipe.id) }
    }

    fun removeFromShoppingList(shoppingListItem: UiShoppingListItem) {
        appExecutors.diskIO().execute {
            db.shoppingListDao.deleteById(shoppingListItem.id)
        }
    }

    fun updatePortionsInShoppingList(recipe: Recipe) {
        Debug.d(this, "Updating portions for '" + recipe.name + "' in shopping list")
        appExecutors.diskIO().execute {
            val recipeId = recipe.id
            val shoppingListItems = db.recipeIngredientDao.findShoppingListItemByRecipeId(recipeId)
            db.shoppingListDao.deleteByRecipeId(recipeId)
            db.shoppingListDao.insert(shoppingListItems)
        }
    }

    fun update(uiShoppingListItem: UiShoppingListItem) {
        Debug.d(this, "Updating shopping list item")
        appExecutors.diskIO().execute {
            val shoppingListItem = db.shoppingListDao.findByIdRaw(uiShoppingListItem.id)
            if (shoppingListItem != null) {
                shoppingListItem.completed = uiShoppingListItem.completed
                Debug.d(this, shoppingListItem.toString())
                db.shoppingListDao.update(shoppingListItem)
            }
        }
    }

    fun deleteAll() {
        appExecutors.diskIO().execute {
            db.tagUsageDao.deleteAll()
            db.recipeTagDao.deleteAll()
            db.recipeStepDao.deleteAll()
            db.recipeIngredientDao.deleteAll()
            db.recipeDao.deleteAll()
            db.ingredientDao.deleteAll()
            db.unitDao.deleteAll()
            db.tagDao.deleteAll()
            db.aisleDao.deleteAll()
        }
    }

    fun addToShoppingList(item: ShoppingListItem) {
        appExecutors.diskIO().execute { db.shoppingListDao.insert(item) }
    }

    fun clearCompletedFromShoppingList() {
        appExecutors.diskIO().execute {
            db.shoppingListDao.hideCompletedRecipeItems()
            db.shoppingListDao.deleteCompletedOrphanItems()
        }
    }

    fun getShoppingListItemById(id: UUID): LiveData<UiShoppingListItem> {
        return db.shoppingListDao.findById(id)
    }

    fun updateShoppingListItem(id: UUID, ingredient: String, quantity: Double?, unitId: UUID?) {
        appExecutors.diskIO().execute {
            val shoppingListItem = db.shoppingListDao.findByIdRaw(id)
            shoppingListItem?.let {
                it.name = ingredient
                it.quantity = quantity
                it.unitId = unitId
                db.shoppingListDao.update(it)
            }
        }
    }

    fun getUiRecipeIngredient(id: UUID): LiveData<UiRecipeIngredient> {
        return db.recipeIngredientDao.findById(id)
    }

    fun updateRecipeIngredient(id: UUID, ingredientName: String, quantity: Double?, unitId: UUID?) {
        Debug.d(this, "updateRecipeIngredient(id, ingredientName, quantity, unitId)")
        if (canFetch()) {
            appExecutors.networkIO().execute {
                var ingredientId: UUID? = db.ingredientDao.getIngredientIdByName(ingredientName)
                if (ingredientId == null) {
                    val ingredient = Ingredient(name = ingredientName)
                    ingredientId = ingredient.id
                    db.ingredientDao.insert(ingredient)
                }
                val oldRecipeIngredient = db.recipeIngredientDao.findByIdRaw(id)
                val newRecipeIngredient = db.recipeIngredientDao.findByIdRaw(id)

                newRecipeIngredient.ingredientId = ingredientId
                newRecipeIngredient.quantity = quantity
                newRecipeIngredient.unitId = unitId

                db.recipeIngredientDao.update(newRecipeIngredient)

                Debug.d(this@RecipeRepository, "Sending request to server.")
                Debug.d(this@RecipeRepository, newRecipeIngredient.toString())
                val call = webservice.updateRecipeIngredient(newRecipeIngredient)
                call.enqueue(object : Callback<ResourceData<RecipeIngredient>> {
                    override fun onResponse(
                        call: Call<ResourceData<RecipeIngredient>>,
                        response: Response<ResourceData<RecipeIngredient>>
                    ) {
                        Debug.d(
                            this@RecipeRepository,
                            "We got a response from the server: " + Gson().toJson(response)
                        )
                        if (response.isSuccessful) {
                            Debug.d(
                                this@RecipeRepository,
                                "Response was successful: " + response.code()
                            )
                            val body = response.body()
                            if (body?.result != null) {
                                Debug.d(
                                    this@RecipeRepository,
                                    "Body contains payload. Update database."
                                )
                            } else {
                                Debug.d(this@RecipeRepository, "Body was empty. Abort.")
                                appExecutors.diskIO()
                                    .execute { db.recipeIngredientDao.update(oldRecipeIngredient) }
                            }
                        } else {
                            Debug.d(
                                this@RecipeRepository,
                                "Response was not successful: " + response.code() + " - " + response.errorBody()
                            )
                            appExecutors.diskIO()
                                .execute { db.recipeIngredientDao.update(oldRecipeIngredient) }
                        }
                    }

                    override fun onFailure(
                        call: Call<ResourceData<RecipeIngredient>>,
                        t: Throwable
                    ) {
                        Debug.d(this@RecipeRepository, "Call failed: " + t.message)
                        appExecutors.diskIO()
                            .execute { db.recipeIngredientDao.update(oldRecipeIngredient) }
                    }
                })
            }
        }
    }
}
