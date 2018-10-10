package com.yeraygarcia.recipes.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.yeraygarcia.recipes.OnWebResponseListener
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import timber.log.Timber
import java.util.*

class RecipeDetailViewModel(private val repository: RecipeDetailRepository, recipeId: UUID) :
    ViewModel() {

    val recipe = repository.getRecipeById(recipeId)
    val recipeIngredients = repository.getIngredientsByRecipeId(recipeId)
    val recipeSteps = repository.getStepsByRecipeId(recipeId)
    val isInShoppingList = repository.isInShoppingList(recipeId)
    var recipeDraft: Recipe? = null
    private lateinit var recipeStep: LiveData<RecipeStep>

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
        recipeDraft = recipeDraft ?: defaultDraft
        recipeDraft?.apply {
            this.duration = try {
                Integer.valueOf(duration.toString()) * 60
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    fun saveSourceToDraft(source: CharSequence, defaultDraft: Recipe?) {
        recipeDraft = recipeDraft ?: defaultDraft
        recipeDraft?.apply { url = source.toString().trim { it <= ' ' } }
    }

    fun persistDraft() {
        Timber.d("persistDraft()")
        recipeDraft?.let {
            repository.persistDraft(it, this)
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
}
