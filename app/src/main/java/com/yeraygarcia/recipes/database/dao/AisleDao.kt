package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Aisle
import java.util.*

@Dao
abstract class AisleDao : BaseDao<Aisle>() {

    @Query("DELETE FROM aisle")
    abstract fun deleteAll()

    @Query("SELECT * FROM aisle")
    abstract fun findAll(): LiveData<List<Aisle>>

    @Query("DELETE FROM aisle WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Aisle>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }
}
