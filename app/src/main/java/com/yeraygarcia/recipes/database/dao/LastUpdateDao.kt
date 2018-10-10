package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

import com.yeraygarcia.recipes.database.entity.LastUpdate

@Dao
abstract class LastUpdateDao : BaseDao<LastUpdate>() {

    @get:Query("SELECT last_update FROM last_update WHERE id = 1")
    abstract val lastUpdate: LiveData<Long>

    @Query("DELETE FROM last_update")
    abstract fun deleteAll()
}
