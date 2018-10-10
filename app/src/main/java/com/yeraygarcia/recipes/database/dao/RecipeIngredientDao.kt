package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.RecipeIngredient
import com.yeraygarcia.recipes.database.entity.ShoppingListItem
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import java.util.*

@Dao
abstract class RecipeIngredientDao : BaseDao<RecipeIngredient>() {

    @Query("DELETE FROM recipe_ingredient")
    abstract fun deleteAll()

    @Query(
        "SELECT ri.id, r.portions, ri.quantity, u.name_singular AS unit_name, u.name_plural AS unit_name_plural, i.name " +
                "FROM recipe_ingredient ri " +
                "INNER JOIN recipe r ON ri.recipe_id = r.id " +
                "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
                "LEFT OUTER JOIN unit u ON ri.unit_id = u.id " +
                "WHERE recipe_id = :recipeId " +
                "ORDER BY sort_order ASC"
    )
    abstract fun findByRecipeId(recipeId: UUID): LiveData<List<UiRecipeIngredient>>

    @Query(
        "SELECT ri.id, ri.recipe_id, ri.ingredient_id, i.name, ri.unit_id, " +
                "r.portions * ri.quantity AS quantity, ri.sort_order, 0 AS completed, 1 AS visible " +
                "FROM recipe_ingredient ri " +
                "INNER JOIN recipe r ON ri.recipe_id = r.id " +
                "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
                "WHERE recipe_id = :recipeId " +
                "ORDER BY sort_order ASC"
    )
    abstract fun findShoppingListItemByRecipeId(recipeId: UUID): List<ShoppingListItem>


    @Query(
        "SELECT ri.id, r.portions, ri.quantity, u.name_singular AS unit_name, u.name_plural AS unit_name_plural, i.name " +
                "FROM recipe_ingredient ri " +
                "INNER JOIN recipe r ON ri.recipe_id = r.id " +
                "INNER JOIN ingredient i ON ri.ingredient_id = i.id " +
                "LEFT OUTER JOIN unit u ON ri.unit_id = u.id " +
                "WHERE ri.id = :id "
    )
    abstract fun findById(id: UUID): LiveData<UiRecipeIngredient>

    @Query("SELECT * FROM recipe_ingredient WHERE id = :id")
    abstract fun findByIdRaw(id: UUID): RecipeIngredient

    @Query("SELECT * FROM recipe_ingredient")
    abstract fun findAll(): LiveData<List<RecipeIngredient>>

    @Query("DELETE FROM recipe_ingredient WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<RecipeIngredient>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }
}
