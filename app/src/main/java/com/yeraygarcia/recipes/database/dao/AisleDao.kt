package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Aisle
import com.yeraygarcia.recipes.database.entity.Tag
import java.util.*

@Dao
abstract class AisleDao : BaseDao<Aisle>() {

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query("SELECT * FROM $TABLE")
    abstract fun findAll(): LiveData<List<Aisle>>

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Aisle>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT * FROM $TABLE")
    abstract fun findAllRaw(): List<Aisle>

    @Query("SELECT * FROM $TABLE WHERE :lastSync = :lastSync")
    abstract fun findModified(lastSync: Long): List<Aisle>

    companion object {
        const val TABLE = "aisle"
    }
}
