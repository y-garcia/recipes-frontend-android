package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Dao
public abstract class RecipeDao extends BaseDao<Recipe> {

    @Query("DELETE FROM recipe")
    public abstract void deleteAll();

    @Query("SELECT * FROM recipe ORDER BY name ASC")
    public abstract LiveData<List<Recipe>> findAll();

    @Query("SELECT count(1) = 0 AS is_empty FROM recipe")
    public abstract boolean isEmpty();

    @Query("SELECT * FROM recipe WHERE id IN (SELECT DISTINCT recipe_id FROM shopping_list_item)")
    public abstract LiveData<List<Recipe>> findRecipesInShoppingList();

    @Query("DELETE FROM recipe WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<UUID> ids);

    public void deleteIfNotIn(List<Recipe> entities) {
        List<UUID> ids = new ArrayList<>();
        for (Recipe entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
