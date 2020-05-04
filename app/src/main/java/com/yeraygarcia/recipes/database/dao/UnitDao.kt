package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Aisle
import com.yeraygarcia.recipes.database.entity.Unit
import java.util.*

@Dao
abstract class UnitDao : BaseDao<Unit>() {

    @get:Query("SELECT name_singular FROM $TABLE UNION SELECT name_plural FROM unit")
    abstract val unitNames: LiveData<List<String>>

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query("SELECT * FROM $TABLE")
    abstract fun findAll(): LiveData<List<Unit>>

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Unit>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT id FROM $TABLE WHERE :name in (name_singular, name_plural)")
    abstract fun findIdByName(name: String): UUID?

    @Query("SELECT * FROM $TABLE WHERE :lastSync = :lastSync")
    abstract fun findModified(lastSync: Long): List<Unit>

    companion object {
        const val TABLE = "unit"
    }
}
