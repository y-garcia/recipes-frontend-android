package com.yeraygarcia.recipes.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeTag
import java.util.*

@Dao
abstract class RecipeTagDao : BaseDao<RecipeTag>() {

    @Query("DELETE FROM $TABLE")
    abstract fun deleteAll()

    @Query("DELETE FROM $TABLE WHERE id IN (:ids)")
    abstract fun deleteByIds(ids: List<UUID>)

    @Query(
        "SELECT r.id, r.name, r.portions, r.duration, r.url " +
                "FROM $TABLE rt " +
                "INNER JOIN ${RecipeDao.TABLE} r ON rt.recipe_id = r.id " +
                "WHERE rt.tag_id IN (:tagIds) " +
                "GROUP BY r.id, r.name, r.portions, r.duration, r.url " +
                "HAVING count(rt.tag_id) = :tagCount"
    )
    abstract fun findRecipesByAllTagIds(tagIds: List<UUID>, tagCount: Int): LiveData<List<Recipe>>

    @Query("SELECT * FROM $TABLE")
    abstract fun findAll(): LiveData<List<RecipeTag>>

    @Query("DELETE FROM $TABLE WHERE id NOT IN (:ids)")
    internal abstract fun deleteIfIdNotIn(ids: List<UUID>)

    fun deleteIfNotIn(entities: List<RecipeTag>) {
        val ids = ArrayList<UUID>()
        for (entity in entities) {
            ids.add(entity.id)
        }
        deleteIfIdNotIn(ids)
    }

    @Query("SELECT * FROM $TABLE WHERE :lastSync = :lastSync")
    abstract fun findModified(lastSync: Long): List<RecipeTag>

    companion object {
        const val TABLE = "recipe_tag"
    }
}
