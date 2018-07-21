package com.yeraygarcia.recipes.database.remote;

import android.arch.lifecycle.LiveData;

import com.yeraygarcia.recipes.database.entity.Aisle;
import com.yeraygarcia.recipes.database.entity.Ingredient;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.RecipeTag;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.database.entity.Unit;
import com.yeraygarcia.recipes.database.entity.custom.All;

import java.util.List;

import retrofit2.http.GET;

public interface Webservice {

    @GET("all")
    LiveData<ApiResponse<ResourceData<All>>> getAll();

    @GET("aisles")
    LiveData<ApiResponse<ResourceData<List<Aisle>>>> getAisles();

    @GET("tags")
    LiveData<ApiResponse<ResourceData<List<Tag>>>> getTags();

    @GET("units")
    LiveData<ApiResponse<ResourceData<List<Unit>>>> getUnits();

    @GET("ingredients")
    LiveData<ApiResponse<ResourceData<List<Ingredient>>>> getIngredients();

    @GET("recipes")
    LiveData<ApiResponse<ResourceData<List<Recipe>>>> getRecipes();

    @GET("recipe-ingredients")
    LiveData<ApiResponse<ResourceData<List<RecipeIngredient>>>> getRecipeIngredients();

    @GET("recipe-steps")
    LiveData<ApiResponse<ResourceData<List<RecipeStep>>>> getRecipeSteps();

    @GET("recipe-tags")
    LiveData<ApiResponse<ResourceData<List<RecipeTag>>>> getRecipeTags();

    @GET("shopping-list-items")
    LiveData<ApiResponse<ResourceData<List<ShoppingListItem>>>> getShoppingListItems();
}
