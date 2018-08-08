package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Ingredient;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class IngredientDao extends BaseDao<Ingredient> {

    @Query("DELETE FROM ingredient")
    public abstract void deleteAll();

    @Query("SELECT * FROM ingredient")
    public abstract LiveData<List<Ingredient>> findAll();

    @Query("DELETE FROM ingredient WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<Long> ids);

    public void deleteIfNotIn(List<Ingredient> entities) {
        List<Long> ids = new ArrayList<>();
        for (Ingredient entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
