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
public abstract class RecipeStepDao implements BaseDao<RecipeStep> {

    @Query("DELETE FROM recipe_step")
    public abstract void deleteAll();

    @Query("SELECT * from recipe_step ORDER BY sort_order ASC")
    abstract LiveData<List<RecipeStep>> findAll();

    @Query("SELECT * from recipe_step WHERE id = :id")
    abstract LiveData<RecipeStep> findById(long id);

    @Query("SELECT * from recipe_step WHERE recipe_id = :recipeId ORDER BY sort_order ASC")
    public abstract LiveData<List<RecipeStep>> findByRecipeId(long recipeId);

}
