package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.yeraygarcia.recipes.database.entity.custom.UiRecipe;

import java.util.List;

@Dao
public abstract class RecipeDetailDao {

    @Query("DELETE FROM recipe")
    public abstract void deleteAll();

    @Transaction
    @Query("SELECT * FROM recipe ORDER BY name ASC")
    public abstract LiveData<List<UiRecipe>> findAll();

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :id")
    public abstract LiveData<UiRecipe> findById(long id);

}
