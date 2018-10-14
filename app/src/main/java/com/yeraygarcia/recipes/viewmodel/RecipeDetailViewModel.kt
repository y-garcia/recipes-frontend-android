package com.yeraygarcia.recipes.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.yeraygarcia.recipes.OnWebResponseListener
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import timber.log.Timber
import java.util.*

class RecipeDetailViewModel(private val repository: RecipeDetailRepository, recipeId: UUID) :
    ViewModel() {

    val recipe = repository.getRecipeById(recipeId)
    val recipeIngredients = repository.getIngredientsByRecipeId(recipeId)
    val recipeSteps = repository.getStepsByRecipeId(recipeId)
    val isInShoppingList = repository.isInShoppingList(recipeId)
    val units = repository.units
    val ingredientNames = repository.ingredientNames
    val unitNames = repository.unitNames

    var recipeDraft: Recipe? = null

    private lateinit var recipeStep: LiveData<RecipeStep>
    private lateinit var recipeIngredient: LiveData<UiRecipeIngredient>

    fun update(recipe: Recipe) {
        Timber.d("update(recipe)")
        repository.update(recipe)
    }

    fun removeFromShoppingList(recipeId: UUID) {
        repository.removeFromShoppingList(recipeId)
    }

    fun addToShoppingList(recipeId: UUID) {
        repository.addToShoppingList(recipeId)
    }

    fun saveDurationToDraft(duration: CharSequence, defaultDraft: Recipe?) {
        recipeDraft = recipeDraft ?: defaultDraft?.copy()
        recipeDraft?.duration = try {
            Integer.valueOf(duration.toString()) * 60
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun saveSourceToDraft(source: CharSequence, defaultDraft: Recipe?) {
        recipeDraft = recipeDraft ?: defaultDraft?.copy()
        recipeDraft?.url = source.toString().trim()
    }

    fun persistDraft(recipe: Recipe?) {
        Timber.d("persistDraft($recipeDraft <-> $recipe)")
        recipeDraft?.let { draft ->
            if (draft != recipe) {
                repository.persistDraft(draft, this)
            }
        }
    }

    fun getRecipeStep(id: UUID): LiveData<RecipeStep> {
        if (!::recipeStep.isInitialized || recipeStep.value?.id !== id) {
            recipeStep = repository.getRecipeStep(id)
        }
        return recipeStep
    }

    fun updateRecipeStep(recipeStep: RecipeStep, listener: OnWebResponseListener) {
        repository.updateRecipeStep(recipeStep, listener)
    }

    fun getIngredient(id: UUID): LiveData<UiRecipeIngredient> {
        if (!::recipeStep.isInitialized || recipeIngredient.value?.id !== id) {
            recipeIngredient = repository.getUiRecipeIngredient(id)
        }
        return recipeIngredient
    }

    fun updateRecipeIngredient(id: UUID, ingredientName: String, quantity: Double?, unitId: UUID?) {
        repository.updateRecipeIngredient(id, ingredientName, quantity, unitId)
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
                "$lowerCaseName = ${unit.nameSingular.toLowerCase()} or ${unit.namePlural.toLowerCase()}"
            if (unit.nameSingular.toLowerCase() == lowerCaseName || unit.namePlural.toLowerCase() == lowerCaseName) {
                Timber.d("$debugMessage ? true")
                return unit.id
            }
            Timber.d("$debugMessage ? false")
        }

        return null
    }
}
