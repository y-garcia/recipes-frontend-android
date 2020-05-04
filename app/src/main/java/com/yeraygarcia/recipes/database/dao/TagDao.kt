package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Tag
import java.util.*

@Dao
abstract class TagDao : BaseDao<Tag>() {

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query("SELECT * FROM $TABLE ORDER BY usage_count DESC, name COLLATE NOCASE ASC")
    abstract fun findAll(): LiveData<List<Tag>>

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Tag>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT * FROM $TABLE WHERE :lastSync = :lastSync")
    abstract fun findModified(lastSync: Long): List<Tag>

    companion object {
        const val TABLE = "tag"
    }
}
