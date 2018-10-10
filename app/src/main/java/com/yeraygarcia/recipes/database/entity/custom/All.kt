package com.yeraygarcia.recipes.database.entity.custom

import com.yeraygarcia.recipes.database.entity.*
import com.yeraygarcia.recipes.database.entity.Unit

data class All(
    var aisles: List<Aisle>,
    var units: List<Unit>,
    var tags: List<Tag>,
    var ingredients: List<Ingredient>,
    var recipes: List<Recipe>,
    var recipeIngredients: List<RecipeIngredient>,
    var recipeSteps: List<RecipeStep>,
    var recipeTags: List<RecipeTag>
)
