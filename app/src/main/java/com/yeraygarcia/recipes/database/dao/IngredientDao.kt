package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Ingredient
import java.util.*

@Dao
abstract class IngredientDao : BaseDao<Ingredient>() {

    @get:Query("SELECT name FROM ingredient UNION SELECT name_singular FROM unit UNION SELECT name_plural FROM unit")
    abstract val unitsAndIngredientNames: LiveData<List<String>>

    @get:Query("SELECT name FROM ingredient")
    abstract val ingredientNames: LiveData<List<String>>

    @Query("DELETE FROM ingredient")
    abstract fun deleteAll()

    @Query("SELECT * FROM ingredient")
    abstract fun findAll(): LiveData<List<Ingredient>>

    @Query("DELETE FROM ingredient WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Ingredient>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT id FROM ingredient WHERE name = :name")
    abstract fun getIngredientIdByName(name: String): UUID
}
