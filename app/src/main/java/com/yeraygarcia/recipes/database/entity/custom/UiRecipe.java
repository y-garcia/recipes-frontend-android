package com.yeraygarcia.recipes.database.entity.custom;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeStep;

import java.util.List;

public class UiRecipe {

    @Embedded
    private Recipe recipe;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    private List<RecipeStep> steps;

    // Getters and setters

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public List<RecipeStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RecipeStep> steps) {
        this.steps = steps;
    }
}
