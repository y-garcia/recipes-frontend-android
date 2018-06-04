package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yeraygarcia.recipes.database.entity.RecipeStep;

import java.util.List;

@Dao
public interface RecipeStepDao {

    @Insert
    long[] insert(RecipeStep... recipeSteps);

    @Update
    int update(RecipeStep... recipeSteps);

    @Delete
    int delete(RecipeStep... recipeSteps);

    @Query("DELETE FROM recipe_step")
    void deleteAll();

    @Query("SELECT * from recipe_step ORDER BY sort_order ASC")
    LiveData<List<RecipeStep>> findAll();

    @Query("SELECT * from recipe_step WHERE id = :id")
    LiveData<RecipeStep> findById(long id);

    @Query("SELECT * from recipe_step WHERE recipe_id = :recipeId ORDER BY sort_order ASC")
    LiveData<List<RecipeStep>> findByRecipeId(long recipeId);

}
