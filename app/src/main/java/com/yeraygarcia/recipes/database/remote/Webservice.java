package com.yeraygarcia.recipes.database.remote;

import android.arch.lifecycle.LiveData;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.All;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface Webservice {

    @POST("tokensignin")
    @Headers("No-Authentication: true")
    Call<ResourceData<User>> postToken(@Query("id_token") String token);

    @GET("all")
    LiveData<ApiResponse<ResourceData<All>>> getAll();

    @GET("recipes")
    LiveData<ApiResponse<ResourceData<List<Recipe>>>> getRecipes();

    @POST("shopping-list-items")
    LiveData<ApiResponse<ResourceData<ShoppingListItem>>> postShoppingListItem(@Body ShoppingListItem item);

    @PUT("recipe-ingredients")
    Call<ResourceData<RecipeIngredient>> updateRecipeIngredient(@Body RecipeIngredient recipeIngredient);
}
