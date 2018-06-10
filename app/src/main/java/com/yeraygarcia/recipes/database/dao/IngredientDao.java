package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Ingredient;

import java.util.List;

@Dao
public abstract class IngredientDao implements BaseDao<Ingredient> {

    @Query("DELETE FROM ingredient")
    public abstract void deleteAll();

}
