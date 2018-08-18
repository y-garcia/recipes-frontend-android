package com.yeraygarcia.recipes.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.yeraygarcia.recipes.database.entity.ShoppingListItem;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;

import java.util.List;
import java.util.UUID;

@Dao
public abstract class ShoppingListDao extends BaseDao<ShoppingListItem> {

    @Query("DELETE FROM shopping_list_item")
    public abstract void deleteAll();

    @Query("DELETE FROM shopping_list_item WHERE recipe_id = :recipeId")
    public abstract void deleteByRecipeId(UUID recipeId);

    @Query("DELETE FROM shopping_list_item WHERE id = :id")
    public abstract void deleteById(UUID id);

    @Query("SELECT " +
            "sli.id, " +
            "sli.quantity, " +
            "u.name_singular AS unit_name, " +
            "u.name_plural AS unit_name_plural, " +
            "sli.name, " +
            "sli.completed, " +
            "a.name as aisle," +
            "r.name as recipe " +
            "FROM shopping_list_item sli " +
            "LEFT OUTER JOIN ingredient i ON sli.ingredient_id = i.id " +
            "LEFT OUTER JOIN aisle a ON i.aisle_id = a.id " +
            "LEFT OUTER JOIN unit u ON sli.unit_id = u.id " +
            "LEFT OUTER JOIN recipe r ON sli.recipe_id = r.id " +
            "WHERE sli.visible = 1 " +
            "ORDER BY sli.completed ASC, a.name, sli.name ASC")
    public abstract LiveData<List<UiShoppingListItem>> findAll();

    @Query("SELECT DISTINCT recipe_id FROM shopping_list_item")
    public abstract LiveData<List<UUID>> findDistinctRecipeIds();


    @Query("SELECT " +
            "sli.id, " +
            "sli.quantity, " +
            "u.name_singular AS unit_name, " +
            "u.name_plural AS unit_name_plural, " +
            "sli.name, " +
            "sli.completed, " +
            "a.name as aisle," +
            "r.name as recipe " +
            "FROM shopping_list_item sli " +
            "LEFT OUTER JOIN ingredient i ON sli.ingredient_id = i.id " +
            "LEFT OUTER JOIN aisle a ON i.aisle_id = a.id " +
            "LEFT OUTER JOIN unit u ON sli.unit_id = u.id " +
            "LEFT OUTER JOIN recipe r ON sli.recipe_id = r.id " +
            "WHERE sli.id = :id")
    public abstract LiveData<UiShoppingListItem> findById(UUID id);

    @Query("SELECT * FROM shopping_list_item WHERE id = :id")
    public abstract ShoppingListItem findByIdRaw(UUID id);

    @Query("SELECT COUNT(DISTINCT recipe_id) FROM shopping_list_item WHERE recipe_id = :recipeId")
    public abstract LiveData<Boolean> isInShoppingList(UUID recipeId);

    @Query("UPDATE shopping_list_item SET visible = 0 WHERE completed = 1 AND recipe_id IS NOT NULL")
    public abstract void hideCompletedRecipeItems();

    @Query("DELETE FROM shopping_list_item WHERE completed = 1 AND recipe_id IS NULL")
    public abstract void deleteCompletedOrphanItems();
}
