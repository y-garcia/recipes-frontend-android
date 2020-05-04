package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.RecipeStep
import java.util.*

@Dao
abstract class RecipeStepDao : BaseDao<RecipeStep>() {

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<RecipeStep>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT * FROM $TABLE")
    abstract fun findAll(): LiveData<List<RecipeStep>>

    @Query("SELECT * FROM $TABLE WHERE recipe_id = :recipeId")
    abstract fun findByRecipeId(recipeId: UUID): LiveData<List<RecipeStep>>

    @Query("SELECT * FROM $TABLE WHERE id = :id")
    abstract fun findById(id: UUID): LiveData<RecipeStep>

    @Query("SELECT * FROM $TABLE WHERE id = :id")
    abstract fun findByIdRaw(id: UUID): RecipeStep

    @Query("SELECT * FROM $TABLE WHERE :lastSync = :lastSync")
    abstract fun findModified(lastSync: Long): List<RecipeStep>

    companion object {
        const val TABLE = "recipe_step"
    }
}
