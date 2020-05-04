package com.yeraygarcia.recipes.database.entity.custom

import com.yeraygarcia.recipes.database.entity.*
import com.yeraygarcia.recipes.database.entity.Unit
import java.util.*

data class SyncDto(
    var lastSync: String,

    var aisles: List<Aisle> = mutableListOf(),
    var units: List<Unit> = mutableListOf(),
    var tags: List<Tag> = mutableListOf(),
    var ingredients: List<Ingredient> = mutableListOf(),
    var recipes: List<Recipe> = mutableListOf(),
    var recipeIngredients: List<RecipeIngredient> = mutableListOf(),
    var recipeSteps: List<RecipeStep> = mutableListOf(),
    var recipeTags: List<RecipeTag> = mutableListOf(),

    var deletedAisles: List<UUID> = mutableListOf(),
    var deletedUnits: List<UUID> = mutableListOf(),
    var deletedTags: List<UUID> = mutableListOf(),
    var deletedIngredients: List<UUID> = mutableListOf(),
    var deletedRecipes: List<UUID> = mutableListOf(),
    var deletedRecipeIngredients: List<UUID> = mutableListOf(),
    var deletedRecipeSteps: List<UUID> = mutableListOf(),
    var deletedRecipeTags: List<UUID> = mutableListOf()
)
