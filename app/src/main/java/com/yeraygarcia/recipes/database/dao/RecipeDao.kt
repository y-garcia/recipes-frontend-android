package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Recipe
import java.util.*

@Dao
abstract class RecipeDao : BaseDao<Recipe>() {

    @get:Query("SELECT count(1) = 0 AS is_empty FROM recipe")
    abstract val isEmpty: Boolean

    @Query("DELETE FROM recipe")
    abstract fun deleteAll()

    @Query("SELECT * FROM recipe ORDER BY name ASC")
    abstract fun findAll(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id = :id")
    abstract fun findById(id: UUID): LiveData<Recipe>

    @Query("SELECT * FROM recipe WHERE id = :id")
    abstract fun findRawById(id: UUID): Recipe

    @Query("SELECT * FROM recipe WHERE id IN (SELECT DISTINCT recipe_id FROM shopping_list_item)")
    abstract fun findRecipesInShoppingList(): LiveData<List<Recipe>>

    @Query("DELETE FROM recipe WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Recipe>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }
}
