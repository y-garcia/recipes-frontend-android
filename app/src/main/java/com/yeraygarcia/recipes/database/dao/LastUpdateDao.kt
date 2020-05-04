package com.yeraygarcia.recipes.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.LastUpdate

@Dao
abstract class LastUpdateDao : BaseDao<LastUpdate>() {

    @get:Query("SELECT last_update FROM last_update WHERE id = 1")
    abstract val lastUpdate: Long

    @get:Query("SELECT datetime(last_update/1000, 'unixepoch') as last_update FROM last_update WHERE id = 1")
    abstract val lastUpdateAsString: String

    @Query("DELETE FROM last_update")
    abstract fun deleteAll()
}
