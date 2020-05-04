package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Ingredient
import java.util.*

@Dao
abstract class IngredientDao : BaseDao<Ingredient>() {

    @get:Query("SELECT name FROM ${IngredientDao.TABLE} UNION SELECT name_singular FROM ${UnitDao.TABLE} UNION SELECT name_plural FROM ${UnitDao.TABLE}")
    abstract val unitsAndIngredientNames: LiveData<List<String>>

    @get:Query("SELECT name FROM ${IngredientDao.TABLE}")
    abstract val ingredientNames: LiveData<List<String>>

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query("SELECT * FROM $TABLE")
    abstract fun findAll(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM $TABLE WHERE id = :id")
    abstract fun findById(id: UUID): Ingredient

    @Query("SELECT id FROM $TABLE WHERE name = :name")
    abstract fun findIdByName(name: String): UUID?

    @Query("SELECT * FROM $TABLE WHERE name = :name")
    abstract fun findByName(name: String): Ingredient?

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Ingredient>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT * FROM $TABLE WHERE modified > :lastSync")
    abstract fun findModified(lastSync: Long): List<Ingredient>

    fun insertIfAbsent(name: String): Ingredient {
        return findByName(name) ?: Ingredient(name = name).apply { insert(this) }
    }

    companion object {
        const val TABLE = "ingredient"
    }
}
