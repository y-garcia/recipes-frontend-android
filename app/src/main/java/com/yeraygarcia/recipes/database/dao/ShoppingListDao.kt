package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.ShoppingListItem
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import java.util.*

@Dao
abstract class ShoppingListDao : BaseDao<ShoppingListItem>() {

    @Query("DELETE FROM shopping_list_item")
    abstract fun deleteAll()

    @Query("DELETE FROM shopping_list_item WHERE recipe_id = :recipeId")
    abstract fun deleteByRecipeId(recipeId: UUID)

    @Query("DELETE FROM shopping_list_item WHERE id = :id")
    abstract fun deleteById(id: UUID)

    @Query(
        "SELECT " +
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
                "ORDER BY sli.completed ASC, a.name, sli.name ASC"
    )
    abstract fun findAll(): LiveData<List<UiShoppingListItem>>

    @Query("SELECT DISTINCT recipe_id FROM shopping_list_item")
    abstract fun findDistinctRecipeIds(): LiveData<List<UUID>>


    @Query(
        "SELECT " +
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
                "WHERE sli.id = :id"
    )
    abstract fun findById(id: UUID): LiveData<UiShoppingListItem>

    @Query("SELECT * FROM shopping_list_item WHERE id = :id")
    abstract fun findByIdRaw(id: UUID): ShoppingListItem?

    @Query("SELECT COUNT(DISTINCT recipe_id) FROM shopping_list_item WHERE recipe_id = :recipeId")
    abstract fun isInShoppingList(recipeId: UUID): LiveData<Boolean>

    @Query("UPDATE shopping_list_item SET visible = 0 WHERE completed = 1 AND recipe_id IS NOT NULL")
    abstract fun hideCompletedRecipeItems()

    @Query("DELETE FROM shopping_list_item WHERE completed = 1 AND recipe_id IS NULL")
    abstract fun deleteCompletedOrphanItems()
}
