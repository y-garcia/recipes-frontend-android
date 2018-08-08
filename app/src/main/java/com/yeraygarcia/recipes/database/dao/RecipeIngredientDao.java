package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class RecipeIngredientDao extends BaseDao<RecipeIngredient> {

    @Query("DELETE FROM recipe_ingredient")
    public abstract void deleteAll();

    @Query("SELECT ri.id, r.portions, ri.quantity, u.name_singular AS unit_name, u.name_plural AS unit_name_plural, i.name " +
            "FROM recipe_ingredient ri " +
            "INNER JOIN recipe r ON ri.recipe_id = r.id " +
            "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
            "LEFT OUTER JOIN unit u ON ri.unit_id = u.id " +
            "WHERE recipe_id = :recipeId " +
            "ORDER BY sort_order ASC")
    public abstract LiveData<List<UiRecipeIngredient>> findByRecipeId(long recipeId);

    @Query("SELECT 0 AS id, ri.recipe_id, ri.ingredient_id, i.name, ri.unit_id, " +
            "r.portions * ri.quantity AS quantity, ri.sort_order, 0 AS completed, 1 AS visible " +
            "FROM recipe_ingredient ri " +
            "INNER JOIN recipe r ON ri.recipe_id = r.id " +
            "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
            "WHERE recipe_id = :recipeId " +
            "ORDER BY sort_order ASC")
    public abstract List<ShoppingListItem> findShoppingListItemByRecipeId(long recipeId);

    @Query("SELECT * FROM recipe_ingredient WHERE id = :id")
    public abstract RecipeIngredient findById(Long id);

    @Query("SELECT * FROM recipe_ingredient")
    public abstract LiveData<List<RecipeIngredient>> findAll();

    @Query("DELETE FROM recipe_ingredient WHERE id NOT IN (:ids)")
    abstract void deleteIfIdNotIn(List<Long> ids);

    public void deleteIfNotIn(List<RecipeIngredient> entities) {
        List<Long> ids = new ArrayList<>();
        for (RecipeIngredient entity : entities) {
            ids.add(entity.getId());
        }
        deleteIfIdNotIn(ids);
    }
}
