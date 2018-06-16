package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;

import java.util.List;

@Dao
public abstract class RecipeIngredientDao implements BaseDao<RecipeIngredient> {

    @Query("DELETE FROM recipe_ingredient")
    public abstract void deleteAll();

    @Query("SELECT r.portions * ri.quantity AS quantity, u.name_singular AS unit_name, u.name_plural AS unit_name_plural, i.name " +
            "FROM recipe_ingredient ri " +
            "INNER JOIN recipe r ON ri.recipe_id = r.id " +
            "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
            "LEFT OUTER JOIN unit u ON ri.unit_id = u.id " +
            "WHERE recipe_id = :recipeId " +
            "ORDER BY sort_order ASC")
    public abstract LiveData<List<UiRecipeIngredient>> findByRecipeId(long recipeId);

    @Query("SELECT 0 as id, ri.recipe_id, ri.ingredient_id, i.name, ri.unit_id, r.portions * ri.quantity AS quantity, ri.sort_order, 0 as completed " +
            "FROM recipe_ingredient ri " +
            "INNER JOIN recipe r ON ri.recipe_id = r.id " +
            "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
            "WHERE recipe_id = :recipeId " +
            "ORDER BY sort_order ASC")
    public abstract List<ShoppingListItem> findShoppingListItemByRecipeId(long recipeId);

}
