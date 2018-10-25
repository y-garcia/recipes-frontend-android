package com.yeraygarcia.recipes.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.database.AppDatabase
import com.yeraygarcia.recipes.database.UUIDTypeConverter
import com.yeraygarcia.recipes.database.entity.*
import com.yeraygarcia.recipes.database.entity.Unit
import com.yeraygarcia.recipes.database.entity.custom.All
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import com.yeraygarcia.recipes.database.remote.*
import com.yeraygarcia.recipes.util.NetworkUtil
import timber.log.Timber
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
                Timber.d("getRecipes().loadFromDb()")
                return db.recipeDao.findAll()
            }

            override fun shouldFetch(data: List<Recipe>?): Boolean {
                Timber.d("getRecipes().shouldFetch(${data?.size ?: "null"})")
                return (data == null || data.isEmpty() || cacheIsDirty) && canFetch()
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<All>>> {
                Timber.d("getRecipes().createCall()")
                return webservice.getAll()
            }

            override fun saveCallResult(callResult: ResourceData<All>) {
                Timber.d("getRecipes().saveCallResult(callResult)")
                updateAllFromResource(callResult)
            }
        }.asLiveData()

    val tags: LiveData<List<Tag>>
        get() {
            Timber.d("getTags()")
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
        Timber.d("getRecipesByTagId($tagIds)")

        return if (tagIds.isEmpty()) {
            recipes
        } else object : NetworkBoundResource<List<Recipe>, ResourceData<All>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Recipe>> {
                Timber.d("getRecipesByTagId($tagIds).loadFromDb()")
                return db.recipeTagDao.findRecipesByAllTagIds(tagIds, tagIds.size)
            }

            override fun shouldFetch(data: List<Recipe>?): Boolean {
                Timber.d("getRecipesByTagId($tagIds).shouldFetch(${data?.size ?: "null"})")
                return cacheIsDirty && canFetch()
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<All>>> {
                Timber.d("getRecipesByTagId($tagIds).createCall()")
                return webservice.getAll()
            }

            override fun saveCallResult(callResult: ResourceData<All>) {
                Timber.d("getRecipesByTagId($tagIds).saveCallResult(callResult)")
                updateAllFromResource(callResult)
            }
        }.asLiveData()

    }

    fun update(recipe: Recipe) {
        appExecutors.diskIO { db.recipeDao.update(recipe) }
    }

    fun logTagUsage(tagId: UUID) {
        val tagUsage = TagUsage(tagId = tagId)
        appExecutors.diskIO { db.tagUsageDao.insert(tagUsage) }
    }

    fun updateTagUsage() {
        appExecutors.diskIO {
            val tags = db.tagUsageDao.tagsWithUpdatedUsage
            db.tagDao.update(tags)
        }
    }

    fun addToShoppingList(recipe: Recipe) {
        Timber.d("Adding '${recipe.name}' to shopping list")
        appExecutors.diskIO {
            val shoppingListItems = db.recipeIngredientDao.findShoppingListItemByRecipeId(recipe.id)
            shoppingListItems.forEach { it.id = UUIDTypeConverter.newUUID() }
            db.shoppingListDao.deleteByRecipeId(recipe.id)
            db.shoppingListDao.insert(shoppingListItems)
        }
    }

    fun removeFromShoppingList(recipe: Recipe) {
        appExecutors.diskIO { db.shoppingListDao.deleteByRecipeId(recipe.id) }
    }

    fun removeFromShoppingList(shoppingListItem: UiShoppingListItem) {
        appExecutors.diskIO {
            db.shoppingListDao.deleteById(shoppingListItem.id)
        }
    }

    fun updatePortionsInShoppingList(recipe: Recipe) {
        Timber.d("Updating portions for '${recipe.name}' in shopping list")
        appExecutors.diskIO {
            val recipeId = recipe.id
            val shoppingListItems = db.recipeIngredientDao.findShoppingListItemByRecipeId(recipeId)
            shoppingListItems.forEach { it.id = UUIDTypeConverter.newUUID() }
            db.shoppingListDao.deleteByRecipeId(recipeId)
            db.shoppingListDao.insert(shoppingListItems)
        }
    }

    fun update(uiShoppingListItem: UiShoppingListItem) {
        Timber.d("Updating shopping list item")
        appExecutors.diskIO {
            val shoppingListItem = db.shoppingListDao.findByIdRaw(uiShoppingListItem.id)
            if (shoppingListItem != null) {
                shoppingListItem.completed = uiShoppingListItem.completed
                Timber.d(shoppingListItem.toString())
                db.shoppingListDao.update(shoppingListItem)
            }
        }
    }

    fun deleteAll() {
        appExecutors.diskIO {
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
        appExecutors.diskIO { db.shoppingListDao.insert(item) }
    }

    fun clearCompletedFromShoppingList() {
        appExecutors.diskIO {
            db.shoppingListDao.hideCompletedRecipeItems()
            db.shoppingListDao.deleteCompletedOrphanItems()
        }
    }

    fun clearAllFromShoppingList() {
        appExecutors.diskIO {
            db.shoppingListDao.hideAllRecipeItems()
            db.shoppingListDao.deleteAllOrphanItems()
        }
    }

    fun getShoppingListItemById(id: UUID): LiveData<UiShoppingListItem> {
        return db.shoppingListDao.findById(id)
    }

    fun updateShoppingListItem(id: UUID, ingredient: String, quantity: Double?, unitId: UUID?) {
        appExecutors.diskIO {
            val shoppingListItem = db.shoppingListDao.findByIdRaw(id)
            shoppingListItem?.let {
                it.name = ingredient
                it.quantity = quantity
                it.unitId = unitId
                db.shoppingListDao.update(it)
            }
        }
    }
}
