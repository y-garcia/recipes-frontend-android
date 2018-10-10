package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Tag
import java.util.*

@Dao
abstract class TagDao : BaseDao<Tag>() {

    @Query("DELETE FROM tag")
    abstract fun deleteAll()

    @Query("SELECT * FROM tag ORDER BY usage_count DESC, name COLLATE NOCASE ASC")
    abstract fun findAll(): LiveData<List<Tag>>

    @Query("DELETE FROM tag WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<Tag>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }
}
