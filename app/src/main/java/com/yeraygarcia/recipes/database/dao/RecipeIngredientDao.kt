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

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query(
        """SELECT ri.id, r.portions, ri.quantity, u.name_singular AS unit_name, u.name_plural AS unit_name_plural, i.name
                FROM $TABLE ri
                INNER JOIN ${RecipeDao.TABLE} r ON ri.recipe_id = r.id
                INNER JOIN ${IngredientDao.TABLE} i ON ri.ingredient_id = i.id
                LEFT OUTER JOIN ${UnitDao.TABLE} u ON ri.unit_id = u.id
                WHERE recipe_id = :recipeId
                ORDER BY sort_order ASC"""
    )
    abstract fun findByRecipeId(recipeId: UUID): LiveData<List<UiRecipeIngredient>>

    @Query(
        """SELECT ri.id, ri.recipe_id, ri.ingredient_id, i.name, ri.unit_id,
                r.portions * ri.quantity AS quantity, ri.sort_order, 0 AS completed, 1 AS visible
                FROM $TABLE ri
                INNER JOIN ${RecipeDao.TABLE} r ON ri.recipe_id = r.id
                INNER JOIN ${IngredientDao.TABLE} i ON ri.ingredient_id = i.id
                WHERE recipe_id = :recipeId
                ORDER BY sort_order ASC"""
    )
    abstract fun findShoppingListItemByRecipeId(recipeId: UUID): List<ShoppingListItem>


    @Query(
        """SELECT ri.id, r.portions, ri.quantity, u.name_singular AS unit_name, u.name_plural AS unit_name_plural, i.name
                FROM $TABLE ri
                INNER JOIN ${RecipeDao.TABLE} r ON ri.recipe_id = r.id
                INNER JOIN ${IngredientDao.TABLE} i ON ri.ingredient_id = i.id
                LEFT OUTER JOIN ${UnitDao.TABLE} u ON ri.unit_id = u.id
                WHERE ri.id = :id"""
    )
    abstract fun findById(id: UUID): LiveData<UiRecipeIngredient>

    @Query("SELECT * FROM $TABLE WHERE id = :id")
    abstract fun findByIdRaw(id: UUID): RecipeIngredient

    @Query("SELECT * FROM $TABLE")
    abstract fun findAll(): LiveData<List<RecipeIngredient>>

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<RecipeIngredient>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT IFNULL(MAX(sort_order) + 1, 1) FROM $TABLE WHERE recipe_id = :recipeId")
    abstract fun getLastPosition(recipeId: UUID): Int

    @Query("SELECT * FROM $TABLE WHERE :lastSync = :lastSync")
    abstract fun findModified(lastSync: Long): List<RecipeIngredient>

    companion object {
        const val TABLE = "recipe_ingredient"
    }
}
