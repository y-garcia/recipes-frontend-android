package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Unit
import java.util.*

@Dao
abstract class UnitDao : BaseDao<Unit>() {

    @get:Query("SELECT name_singular FROM unit UNION SELECT name_plural FROM unit")
    abstract val unitNames: LiveData<List<String>>

    @Query("DELETE FROM unit")
    abstract fun deleteAll()

    @Query("SELECT * FROM unit")
    abstract fun findAll(): LiveData<List<Unit>>

    @Query("DELETE FROM unit WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Unit>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }
}
