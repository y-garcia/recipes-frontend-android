package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.RecipeStep
import java.util.*

@Dao
abstract class RecipeStepDao : BaseDao<RecipeStep>() {

    @Query("DELETE FROM recipe_step")
    abstract fun deleteAll()

    @Query("DELETE FROM recipe_step WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<RecipeStep>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT * FROM recipe_step")
    abstract fun findAll(): LiveData<List<RecipeStep>>

    @Query("SELECT * FROM recipe_step WHERE recipe_id = :recipeId")
    abstract fun findByRecipeId(recipeId: UUID): LiveData<List<RecipeStep>>

    @Query("SELECT * FROM recipe_step WHERE id = :id")
    abstract fun findById(id: UUID): LiveData<RecipeStep>

    @Query("SELECT * FROM recipe_step WHERE id = :id")
    abstract fun findByIdRaw(id: UUID): RecipeStep
}
