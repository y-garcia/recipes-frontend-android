package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.RecipeStep;

import java.util.List;

@Dao
public abstract class RecipeStepDao extends BaseDao<RecipeStep> {

    @Query("DELETE FROM recipe_step")
    public abstract void deleteAll();

    @Query("SELECT * FROM recipe_step")
    public abstract LiveData<List<RecipeStep>> findAll();
}
