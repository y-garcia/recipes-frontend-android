package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.RecipeStep;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Dao
public abstract class RecipeStepDao extends BaseDao<RecipeStep> {

    @Query("DELETE FROM recipe_step")
    public abstract void deleteAll();

    @Query("SELECT * FROM recipe_step")
    public abstract LiveData<List<RecipeStep>> findAll();

    @Query("DELETE FROM recipe_step WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<UUID> ids);

    public void deleteIfNotIn(List<RecipeStep> entities) {
        List<UUID> ids = new ArrayList<>();
        for (RecipeStep entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
