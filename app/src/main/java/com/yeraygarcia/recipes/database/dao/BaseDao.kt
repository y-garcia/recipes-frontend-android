package com.yeraygarcia.recipes.database.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.IGNORE
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update
import java.util.*

abstract class BaseDao<T> {

    @Delete
    abstract fun delete(entity: T): Int

    @Delete
    abstract fun delete(entities: List<T>): Int

    @Insert
    abstract fun insert(entity: T): Long

    @Insert
    abstract fun insert(entities: List<T>): List<Long>

    @Insert(onConflict = IGNORE)
    internal abstract fun insertOrIgnore(entities: List<T>): List<Long>

    @Update
    abstract fun update(entity: T)

    @Update
    abstract fun update(entities: List<T>)

    @Transaction
    open fun upsert(entity: T) {
        upsert(listOf(entity))
    }

    @Transaction
    open fun upsert(entities: List<T>) {
        val insertResult = insertOrIgnore(entities)
        val updateList = ArrayList<T>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(entities[i])
            }
        }

        if (!updateList.isEmpty()) {
            update(updateList)
        }
    }
}
