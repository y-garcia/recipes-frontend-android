package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yeraygarcia.recipes.database.entity.RecipeIngredient;

import java.util.List;

@Dao
public interface RecipeIngredientDao {

    @Insert
    long[] insert(RecipeIngredient... recipeIngredients);

    @Update
    int update(RecipeIngredient... recipeIngredients);

    @Delete
    int delete(RecipeIngredient... recipeIngredients);

    @Query("DELETE FROM recipe_ingredient")
    void deleteAll();

    @Query("SELECT * from recipe_ingredient ORDER BY sort_order ASC")
    LiveData<List<RecipeIngredient>> findAll();

    @Query("SELECT * from recipe_ingredient WHERE id = :id")
    LiveData<RecipeIngredient> findById(long id);

    @Query("SELECT * from recipe_ingredient WHERE recipe_id = :recipeId ORDER BY sort_order ASC")
    LiveData<List<RecipeIngredient>> findByRecipeId(long recipeId);

}
