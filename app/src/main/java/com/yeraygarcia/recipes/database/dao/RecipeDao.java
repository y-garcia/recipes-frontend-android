package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.Recipe;

import java.util.List;

@Dao
public abstract class RecipeDao extends BaseDao<Recipe> {

    @Query("DELETE FROM recipe")
    public abstract void deleteAll();

    @Query("SELECT * FROM recipe ORDER BY name ASC")
    public abstract LiveData<List<Recipe>> findAll();

    @Query("SELECT count(1) FROM recipe")
    public abstract int getRecipeCount();

    @Query("SELECT count(1) = 0 AS is_empty FROM recipe")
    public abstract boolean isEmpty();

    @Query("SELECT * FROM recipe WHERE id IN (SELECT DISTINCT recipe_id FROM shopping_list_item)")
    public abstract LiveData<List<Recipe>> findRecipesInShoppingList();
}
