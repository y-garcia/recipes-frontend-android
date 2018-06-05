package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.yeraygarcia.recipes.database.entity.custom.RecipeDetail;

import java.util.List;

@Dao
public abstract class RecipeDetailDao {

    @Query("DELETE FROM recipe")
    public abstract void deleteAll();

    @Transaction
    @Query("SELECT * FROM recipe ORDER BY name ASC")
    abstract LiveData<List<RecipeDetail>> findAll();

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :id")
    public abstract LiveData<RecipeDetail> findById(long id);

}
