package com.yeraygarcia.recipes.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Deleted
import java.util.*

@Dao
abstract class DeletedDao : BaseDao<Deleted>() {

    @Query("SELECT deletedId FROM deleted WHERE deleted > :lastSync AND tableId = :tableId")
    abstract fun findDeleted(lastSync: Long, tableId: Int): List<UUID>

    companion object {
        const val TABLE = "deleted"
        const val TABLE_AISLE = 1
        const val TABLE_DELETED = 2
        const val TABLE_DELETED_TABLE = 3
        const val TABLE_INGREDIENT = 4
        const val TABLE_OAUTH = 5
        const val TABLE_PLACEMENT = 6
        const val TABLE_RECIPE = 7
        const val TABLE_RECIPE_INGREDIENT = 8
        const val TABLE_RECIPE_STEP = 9
        const val TABLE_RECIPE_TAG = 10
        const val TABLE_RECIPE_USER = 11
        const val TABLE_STORE = 12
        const val TABLE_SYNC = 13
        const val TABLE_TAG = 14
        const val TABLE_UNIT = 15
        const val TABLE_USER = 16
    }
}
