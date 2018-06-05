package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.RecipeIngredient;

import java.util.List;

@Dao
public abstract class RecipeIngredientDao implements BaseDao<RecipeIngredient> {

    @Query("DELETE FROM recipe_ingredient")
    public abstract void deleteAll();

    @Query("SELECT * from recipe_ingredient ORDER BY sort_order ASC")
    abstract LiveData<List<RecipeIngredient>> findAll();

    @Query("SELECT * from recipe_ingredient WHERE id = :id")
    abstract LiveData<RecipeIngredient> findById(long id);

    @Query("SELECT * from recipe_ingredient WHERE recipe_id = :recipeId ORDER BY sort_order ASC")
    public abstract LiveData<List<RecipeIngredient>> findByRecipeId(long recipeId);

}
