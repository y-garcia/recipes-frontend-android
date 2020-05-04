package com.yeraygarcia.recipes.database.remote

import android.arch.lifecycle.LiveData
import com.yeraygarcia.recipes.database.entity.Ingredient
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeIngredient
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.entity.custom.All
import com.yeraygarcia.recipes.database.entity.custom.SyncDto
import retrofit2.Call
import retrofit2.http.*

interface Webservice {

    @POST("tokensignin")
    @Headers("No-Authentication: true")
    fun postToken(@Query("id_token") token: String): Call<ResourceData<User>>

    @GET("all")
    fun getAll(): LiveData<ApiResponse<ResourceData<All>>>

    @PUT("recipes")
    fun updateRecipe(@Body recipe: Recipe): Call<ResourceData<Recipe>>

    @PUT("recipe-ingredients")
    fun updateRecipeIngredient(@Body recipeIngredient: RecipeIngredient): Call<ResourceData<RecipeIngredient>>

    @PUT("recipe-steps")
    fun updateRecipeStep(@Body recipeStep: RecipeStep): Call<ResourceData<RecipeStep>>

    @POST("ingredients")
    fun insertIngredient(@Body ingredient: Ingredient): Call<ResourceData<Ingredient>>

    @POST("recipe-ingredients")
    fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Call<ResourceData<RecipeIngredient>>

    @POST("sync")
    fun sync(@Body dataToSync: SyncDto): Call<ResourceData<SyncDto>>
}
