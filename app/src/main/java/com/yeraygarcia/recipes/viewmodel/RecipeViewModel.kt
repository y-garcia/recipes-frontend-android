package com.yeraygarcia.recipes.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.LongSparseArray
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.ShoppingListItem
import com.yeraygarcia.recipes.database.entity.Tag
import com.yeraygarcia.recipes.database.entity.Unit
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import com.yeraygarcia.recipes.database.remote.Resource
import com.yeraygarcia.recipes.database.repository.RecipeRepository
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)

    private val mTagFilter = MutableLiveData<List<UUID>>()
    private val mRecipes =
        Transformations.switchMap(mTagFilter) { tagIds -> repository.getRecipesByTagId(tagIds) }
    private val mTags: LiveData<List<Tag>>
    val recipeIdsInShoppingList: LiveData<List<UUID>>
    val recipesInShoppingList: LiveData<List<Recipe>>
    val shoppingListItems: LiveData<List<UiShoppingListItem>>
    val units: LiveData<List<Unit>>

    private val mShoppingListItemsDraft = MutableLiveData<LongSparseArray<UiShoppingListItem>>()
    private var mShoppingListItem: LiveData<UiShoppingListItem>? = null
    val unitsAndIngredientNames: LiveData<List<String>>
    val ingredientNames: LiveData<List<String>>
    val unitNames: LiveData<List<String>>
    private var mRecipeIngredient: LiveData<UiRecipeIngredient>? = null

    val recipes: LiveData<Resource<List<Recipe>>>
        get() {
            Timber.d("getRecipes()")
            return mRecipes
        }

    val tags: LiveData<List<Tag>>
        get() {
            Timber.d("getTags()")
            return mTags
        }

    val tagFilter: MutableLiveData<List<UUID>>
        get() {
            Timber.d("getTagFilter()")
            return mTagFilter
        }

    val tagFilterAsArray: ArrayList<String>
        get() {
            Timber.d("getTagFilterAsArray()")

            val tagFilter = mTagFilter.value ?: return ArrayList()

            val result = ArrayList<String>(tagFilter.size)
            for (uuid in tagFilter) {
                result.add(uuid.toString())
            }
            return result
        }

    private val unitRegex: String
        get() {
            val units = this.units.value ?: return "[a-zäöü]+"

            val regex = StringBuilder()

            for (unit in units) {
                regex.append(unit.nameSingular).append("|")

                if (unit.nameSingular != unit.namePlural) {
                    regex.append(unit.namePlural).append("|")
                }
            }

            regex.deleteCharAt(regex.length - 1)

            return regex.toString()
        }

    init {
        Timber.d("RecipeViewModel(application)")

        mTags = repository.tags
        mTagFilter.value = ArrayList()

        recipeIdsInShoppingList = repository.recipeIdsInShoppingList
        recipesInShoppingList = repository.recipesInShoppingList
        shoppingListItems = repository.shoppingListItems
        mShoppingListItemsDraft.value = LongSparseArray()
        units = repository.units
        unitsAndIngredientNames = repository.unitsAndIngredientNames
        ingredientNames = repository.ingredientNames
        unitNames = repository.unitNames
    }

    fun addTagToFilter(tag: Tag) {
        Timber.d("addTagToFilter(${tag.id})")
        val newTags = mTagFilter.value as MutableList<UUID>? ?: ArrayList()
        newTags.add(tag.id)
        mTagFilter.value = newTags
        repository.logTagUsage(tag.id)
    }

    fun removeTagFromFilter(tag: Tag) {
        Timber.d("removeTagFromFilter(${tag.id})")
        val newTags = mTagFilter.value as MutableList<UUID>? ?: ArrayList()
        newTags.remove(tag.id)
        mTagFilter.value = newTags
    }

    fun setTagFilter(tag: Tag) {
        Timber.d("setSelectedTagIds(tag)")
        val tagFilterList = ArrayList<UUID>()
        tagFilterList.add(tag.id)
        mTagFilter.value = tagFilterList
        repository.logTagUsage(tag.id)
    }

    fun setTagFilter(tagFilter: Array<String>?) {
        Timber.d("setSelectedTagIds(tagFilter)")
        if (tagFilter != null) {
            val uuids = ArrayList<UUID>(tagFilter.size)
            for (uuidString in tagFilter) {
                uuids.add(UUID.fromString(uuidString))
            }
            mTagFilter.value = uuids
        }
    }

    fun updateTagUsage() {
        Timber.d("updateTagUsage()")
        repository.updateTagUsage()
    }

    fun addRecipeToShoppingList(recipe: Recipe) {
        repository.addToShoppingList(recipe)
    }

    fun updatePortionsInShoppingList(recipe: Recipe) {
        repository.update(recipe)
        repository.updatePortionsInShoppingList(recipe)
    }

    fun deleteRecipeFromShoppingList(recipe: Recipe) {
        repository.removeFromShoppingList(recipe)
    }

    fun markComplete(shoppingListItem: UiShoppingListItem, complete: Boolean) {
        shoppingListItem.completed = complete
        repository.update(shoppingListItem)
    }

    fun removeFromShoppingList(shoppingListItem: UiShoppingListItem) {
        repository.removeFromShoppingList(shoppingListItem)
    }

    fun deleteAll() {
        Timber.d("deleteAll()")
        repository.deleteAll()
    }

    fun refreshAll() {
        repository.invalidateCache()
        forceRefresh()
    }

    private fun clearTagFilter() {
        mTagFilter.value = mutableListOf()
    }

    private fun forceRefresh() {
        // TODO this is a hack :-(
        val tagFilterCopy = mTagFilter.value
        clearTagFilter()
        mTagFilter.value = tagFilterCopy
    }

    fun addItemToShoppingList(newItem: String?) {
        if (newItem != null && !newItem.trim().isEmpty()) {
            val name = newItem.trim()

            val units = unitRegex
            val matcher1 = Pattern.compile(
                "([0-9]+(?:(?:.|,)[0-9]+)?) ?($units) (.+)$",
                Pattern.CASE_INSENSITIVE
            ).matcher(name)
            val matcher2 =
                Pattern.compile("([0-9]+(?:(?:.|,)[0-9]+)?) (.+)$", Pattern.CASE_INSENSITIVE)
                    .matcher(name)

            val item = when {
                matcher1.matches() -> {
                    val q = matcher1.group(1).replace(",", ".")
                    val quantity = java.lang.Double.valueOf(q)
                    val unit = getUnitIdByName(matcher1.group(2))
                    val ingredient = matcher1.group(3)

                    ShoppingListItem(name = ingredient, quantity = quantity, unitId = unit)
                }
                matcher2.matches() -> {
                    val q = matcher2.group(1).replace(",", ".")
                    val quantity = java.lang.Double.valueOf(q)
                    val ingredient = matcher2.group(2)

                    ShoppingListItem(name = ingredient, quantity = quantity)
                }
                else -> ShoppingListItem(name = name)
            }

            repository.addToShoppingList(item)
        }
    }

    fun getUnitIdByName(unitName: String?): UUID? {
        Timber.d("getUnitIdByName(${unitName ?: "null"})")
        val units = this.units.value

        if (units == null || unitName == null || unitName.isEmpty()) {
            return null
        }

        val lowerCaseName = unitName.toLowerCase()

        for (unit in units) {
            val debugMessage =
                lowerCaseName + " = " + unit.nameSingular.toLowerCase() + " or " + unit.namePlural.toLowerCase() + " ? "
            if (unit.nameSingular.toLowerCase() == lowerCaseName || unit.namePlural.toLowerCase() == lowerCaseName) {
                Timber.d("${debugMessage}true")
                return unit.id
            }
            Timber.d("${debugMessage}false")
        }

        return null
    }

    fun clearCompletedFromShoppingList() {
        repository.clearCompletedFromShoppingList()
    }

    fun getShoppingListItem(id: UUID): LiveData<UiShoppingListItem> {
        if (mShoppingListItem == null || mShoppingListItem!!.value != null && mShoppingListItem!!.value!!.id !== id) {
            mShoppingListItem = repository.getShoppingListItemById(id)
        }
        return mShoppingListItem as LiveData<UiShoppingListItem>
    }

    fun updateShoppingListItem(id: UUID, ingredient: String, quantity: Double?, unitId: UUID?) {
        repository.updateShoppingListItem(id, ingredient, quantity, unitId)
    }

    fun getIngredient(id: UUID): LiveData<UiRecipeIngredient> {
        if (mRecipeIngredient == null || mRecipeIngredient!!.value != null && mRecipeIngredient!!.value!!.id !== id) {
            mRecipeIngredient = repository.getUiRecipeIngredient(id)
        }
        return mRecipeIngredient as LiveData<UiRecipeIngredient>
    }

    fun updateRecipeIngredient(
        id: UUID,
        ingredientName: String,
        quantity: Double?,
        unitId: UUID?
    ) {
        repository.updateRecipeIngredient(id, ingredientName, quantity, unitId)
    }
}
