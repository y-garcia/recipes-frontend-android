package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Recipe;

import java.util.List;

@Dao
public abstract class RecipeDao implements BaseDao<Recipe> {

    @Query("DELETE FROM recipe")
    public abstract void deleteAll();

    @Query("SELECT * FROM recipe ORDER BY name ASC")
    public abstract LiveData<List<Recipe>> findAll();

}
