package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;

import java.util.List;

@Dao
public abstract class ShoppingListDao extends BaseDao<ShoppingListItem> {

    @Query("DELETE FROM shopping_list_item")
    public abstract void deleteAll();

    @Query("DELETE FROM shopping_list_item WHERE recipe_id = :recipeId")
    public abstract void deleteByRecipeId(long recipeId);

    @Query("DELETE FROM shopping_list_item WHERE id = :itemId")
    public abstract void deleteById(long itemId);

    @Query("SELECT " +
            "sli.id, " +
            "sli.quantity, " +
            "u.name_singular AS unit_name, " +
            "u.name_plural AS unit_name_plural, " +
            "sli.name, " +
            "sli.completed, " +
            "a.name as aisle " +
            "FROM shopping_list_item sli " +
            "LEFT OUTER JOIN ingredient i ON sli.ingredient_id = i.id " +
            "LEFT OUTER JOIN aisle a ON i.aisle_id = a.id " +
            "LEFT OUTER JOIN unit u ON sli.unit_id = u.id " +
            "ORDER BY a.name, sli.name ASC")
    public abstract LiveData<List<UiShoppingListItem>> findAll();

    @Query("SELECT DISTINCT recipe_id FROM shopping_list_item")
    public abstract LiveData<List<Long>> findDistinctRecipeIds();

    @Query("SELECT * FROM shopping_list_item WHERE id = :id")
    public abstract ShoppingListItem findById(Long id);

    @Query("SELECT COUNT(DISTINCT recipe_id) FROM shopping_list_item WHERE recipe_id = :recipeId")
    public abstract LiveData<Boolean> isInShoppingList(long recipeId);
}
