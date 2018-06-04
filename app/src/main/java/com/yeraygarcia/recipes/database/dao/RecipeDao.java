package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.yeraygarcia.recipes.database.entity.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    long[] insert(Recipe... recipes);

    @Update
    int update(Recipe... recipes);

    @Delete
    int delete(Recipe... recipes);

    @Query("DELETE FROM recipe")
    void deleteAll();

    @Query("SELECT * from recipe ORDER BY name ASC")
    LiveData<List<Recipe>> getAllRecipes();

    @Query("SELECT * from recipe WHERE id = :id")
    LiveData<Recipe> getRecipeById(long id);

}
