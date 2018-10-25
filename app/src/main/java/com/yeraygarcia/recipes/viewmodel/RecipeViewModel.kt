package com.yeraygarcia.recipes.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.ShoppingListItem
import com.yeraygarcia.recipes.database.entity.Tag
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import com.yeraygarcia.recipes.database.remote.Resource
import com.yeraygarcia.recipes.database.repository.RecipeRepository
import com.yeraygarcia.recipes.testing.OpenForTesting
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@OpenForTesting
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)

    val recipeIdsInShoppingList = repository.recipeIdsInShoppingList
    val recipesInShoppingList = repository.recipesInShoppingList
    val shoppingListItems = repository.shoppingListItems
    val units = repository.units
    val unitsAndIngredientNames = repository.unitsAndIngredientNames
    val ingredientNames = repository.ingredientNames
    val unitNames = repository.unitNames

    private lateinit var shoppingListItem: LiveData<UiShoppingListItem>

    final val tagFilter = MutableLiveData<List<UUID>>().apply { value = ArrayList() }
        get() {
            Timber.d("getTagFilter()")
            return field
        }

    val recipes: LiveData<Resource<List<Recipe>>> =
        Transformations.switchMap(tagFilter) { tagIds -> repository.getRecipesByTagId(tagIds) }
        get() {
            Timber.d("getRecipes()")
            return field
        }

    val tags = repository.tags
        get() {
            Timber.d("getTags()")
            return field
        }

    val tagFilterAsArray: ArrayList<String>
        get() {
            Timber.d("getTagFilterAsArray()")

            val tagFilter = tagFilter.value ?: return ArrayList()

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

    fun addTagToFilter(tag: Tag) {
        Timber.d("addTagToFilter(${tag.id})")
        val newTags = tagFilter.value as MutableList<UUID>? ?: ArrayList()
        newTags.add(tag.id)
        tagFilter.value = newTags
        repository.logTagUsage(tag.id)
    }

    fun removeTagFromFilter(tag: Tag) {
        Timber.d("removeTagFromFilter(${tag.id})")
        val newTags = tagFilter.value as MutableList<UUID>? ?: ArrayList()
        newTags.remove(tag.id)
        tagFilter.value = newTags
    }

    fun setTagFilter(tag: Tag) {
        Timber.d("setSelectedTagIds(tag)")
        val tagFilterList = ArrayList<UUID>()
        tagFilterList.add(tag.id)
        tagFilter.value = tagFilterList
        repository.logTagUsage(tag.id)
    }

    fun setTagFilter(tagFilter: Array<String>?) {
        Timber.d("setSelectedTagIds(tagFilter)")
        if (tagFilter != null) {
            val uuids = ArrayList<UUID>(tagFilter.size)
            for (uuidString in tagFilter) {
                uuids.add(UUID.fromString(uuidString))
            }
            this.tagFilter.value = uuids
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
        tagFilter.value = mutableListOf()
    }

    private fun forceRefresh() {
        // TODO this is a hack :-(
        val tagFilterCopy = tagFilter.value
        clearTagFilter()
        tagFilter.value = tagFilterCopy
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
        if (!::shoppingListItem.isInitialized || shoppingListItem.value?.id !== id) {
            shoppingListItem = repository.getShoppingListItemById(id)
        }
        return shoppingListItem
    }

    fun updateShoppingListItem(id: UUID, ingredient: String, quantity: Double?, unitId: UUID?) {
        repository.updateShoppingListItem(id, ingredient, quantity, unitId)
    }
}
