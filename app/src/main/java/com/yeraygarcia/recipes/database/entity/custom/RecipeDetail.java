package com.yeraygarcia.recipes.database.entity.custom;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;

import java.util.List;

public class RecipeDetail {

    @Embedded
    private Recipe recipe;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    private List<RecipeIngredient> ingredients;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    private List<RecipeStep> steps;

    // Getters and setters

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<RecipeStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RecipeStep> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return recipe.toString() +
                "\n\n" + ingredients.toString() +
                "\n\n" + steps.toString();
    }
}
